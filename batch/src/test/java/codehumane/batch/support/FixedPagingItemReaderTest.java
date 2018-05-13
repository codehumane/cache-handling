package codehumane.batch.support;

import lombok.val;
import org.junit.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FixedPagingItemReaderTest {

    @Test
    public void read_읽어들일_데이터가_0개라면_처음부터_null을_반환해야_함() {

        // given
        val page = new PageImpl<String>(emptyList(), new PageRequest(0, 100), 0);
        val reader = new FixedPagingItemReader<String>(() -> page);

        // when
        val read = reader.read();

        // then
        assertNull(read);
    }

    @Test
    public void read_읽어들일_데이터가_1개라면_첫번째에는_데이터를_반환해야_함() {

        // given
        val item = "1";
        val page = new PageImpl<String>(singletonList(item), new PageRequest(0, 100), 1);
        val reader = new FixedPagingItemReader<String>(() -> page);

        // when
        val read1st = reader.read();

        // then
        assertEquals(item, read1st);
    }

    @Test
    public void read_읽어들일_데이터가_1개라면_두번째에는_null을_반환해야_함() {

        // given
        val item = "1";
        val page = new PageImpl<String>(singletonList(item), new PageRequest(0, 100), 1);
        val reader = new FixedPagingItemReader<String>(() -> page);

        // and
        reader.read();

        // when
        val read2nd = reader.read();

        // then
        assertNull(read2nd);
    }


    @Test
    public void read_읽어들일_데이터_갯수와_페이지_크기가_동일하다면_페이지_크기_번째의_시도에는_마지막_데이터를_반환해야_함() {

        // given
        val item3rd = "3";
        val page = new PageImpl<String>(asList("1", "2", item3rd), new PageRequest(0, 3), 3);
        val reader = new FixedPagingItemReader<String>(() -> page);

        // and
        reader.read();
        reader.read();

        // when
        val read3rd = reader.read();

        // then
        assertEquals(item3rd, read3rd);
    }

    @Test
    public void read_읽어들일_데이터_갯수와_페이지_크기가_동일하다면_페이지_크기보다_큰_시도에는_null을_반환해야_함() {

        // given
        val page = new PageImpl<String>(asList("1", "2", "3"), new PageRequest(0, 3), 3);
        val reader = new FixedPagingItemReader<String>(() -> page);

        // and
        reader.read();
        reader.read();
        reader.read();

        // when
        val read4th = reader.read();

        // then
        assertNull(read4th);
    }

    @Test
    public void read_페이지_크기보다는_작고_주어진_데이터_수보다_큰_횟수의_시도에는_null을_반환해야_함() {

        // given
        val page = new PageImpl<String>(asList("1", "2"), new PageRequest(0, 3), 2);
        val reader = new FixedPagingItemReader<String>(() -> page);

        // and
        reader.read();
        reader.read();

        // when
        val read3rd = reader.read();

        // then
        assertNull(read3rd);
    }

    @Test
    public void read_읽어들일_데이터_갯수가_페이지_사이즈_보다_큰_경우() {

        // given
        val item1 = "1";
        val item2 = "2";
        val item3 = "3";

        // and
        val page = new PageImpl<String>(asList(item1, item2, item3), new PageRequest(0, 3), 6);
        val reader = new FixedPagingItemReader<String>(() -> page);

        // then
        assertEquals(item1, reader.read());
        assertEquals(item2, reader.read());
        assertEquals(item3, reader.read());
        assertEquals("페이지 번호가 고정 되어 있으므로 1부터 반복해서 가져옴", item1, reader.read());
        assertEquals("페이지 번호가 고정 되어 있으므로 1부터 반복해서 가져옴", item2, reader.read());
        assertEquals("페이지 번호가 고정 되어 있으므로 1부터 반복해서 가져옴", item3, reader.read());
        assertNull("페이지 번호는 고정되어 있지만, 조회된 총 데이터 수 만큼 가져오므로, 더 이상 읽어들일 데이터 없음", reader.read());
    }

    @Test
    public void read_읽어들일_데이터_갯수가_페이지_사이즈_보다_크고_시작_페이지가_0보다_큰_경우() {

        // given
        val item1 = "1";
        val item2 = "2";
        val item3 = "3";

        // and
        val page = new PageImpl<String>(asList(item1, item2, item3), new PageRequest(1, 3), 6);
        val reader = new FixedPagingItemReader<String>(() -> page);

        // then
        assertEquals(item1, reader.read());
        assertEquals(item2, reader.read());
        assertEquals(item3, reader.read());
        assertNull("최초의 총 데이터 수는 6이지만, 시작 페이지 번호가 1이므로, 더 이상 읽어 들일 데이터 없음", reader.read());
    }
}
