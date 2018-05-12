package codehumane.batch.support;

import static java.util.Objects.nonNull;

import lombok.val;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Supplier;

/**
 * 페이지 단위로 쪼개서 데이터를 가져오지만, 페이지 번호를 증가시키지는 않는 {@link ItemReader}
 * <p>
 * `{@link org.springframework.batch.item.data.RepositoryItemReader}#doRead` 함께 참고.
 * synchronized나 데이터 초기화 등을 하지 않으므로, 매번 객체를 생성해서 사용해야 함에 유의.
 */
public class FixedPagingItemReader<T> implements ItemReader<T> {

    private final Supplier<Page<T>> pageSupplier;

    private Integer maxPageNumber;
    private Integer currentPageNumber;
    private Integer currentItemIndexOfPage;
    private List<T> items;

    public FixedPagingItemReader(Supplier<Page<T>> pageSupplier) {
        this.pageSupplier = pageSupplier;
    }

    @Override
    public T read() {

        if (items == null || currentItemIndexOfPage >= items.size()) {
            if (nonNull(currentPageNumber) && nonNull(maxPageNumber)
                    && currentPageNumber > maxPageNumber) return null;

            val currentPage = pageSupplier.get();

            if (maxPageNumber == null)
                maxPageNumber = currentPage.getTotalPages() - 1;

            if (currentPageNumber == null)
                currentPageNumber = currentPage.getNumber();

            items = currentPage.getContent();
            currentItemIndexOfPage = 0;
            currentPageNumber++;
        }

        if (items.size() > currentItemIndexOfPage)
            return items.get(currentItemIndexOfPage++);

        return null;
    }

}
