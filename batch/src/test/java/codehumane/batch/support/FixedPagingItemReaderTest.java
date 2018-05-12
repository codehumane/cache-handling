package codehumane.batch.support;

import lombok.val;
import org.junit.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FixedPagingItemReaderTest {

    @Test
    public void read_0개인_경우() {

        // given
        val reader = new FixedPagingItemReader<String>(() ->
                new PageImpl<>(Collections.emptyList(), new PageRequest(0, 100), 0));

        // then
        assertNull("읽어들인 대상이 없으므로 null 반환", reader.read());
    }

    @Test
    public void read_1개인_경우() {

        // given
        val reader = new FixedPagingItemReader<String>(() ->
                new PageImpl<>(Collections.singletonList("1st"), new PageRequest(0, 100), 1));

        // then
        assertEquals("1st", reader.read());
        assertNull("더 이상 읽어들일 데이터가 없음", reader.read());
    }

    @Test
    public void read_읽어들일_데이터_갯수와_페이지_사이즈가_동일한_경우() {

        // given
        val reader = new FixedPagingItemReader<String>(() ->
                new PageImpl<>(Arrays.asList("1", "2", "3", "4", "5"), new PageRequest(0, 5), 5));

        // then
        assertEquals("1", reader.read());
        assertEquals("2", reader.read());
        assertEquals("3", reader.read());
        assertEquals("4", reader.read());
        assertEquals("5", reader.read());
        assertNull("더 이상 읽어들일 데이터가 없음", reader.read());
    }

    @Test
    public void read_읽어들일_데이터_갯수가_페이지_사이즈_보다_큰_경우() {

        // given
        val reader = new FixedPagingItemReader<String>(() ->
                new PageImpl<>(Arrays.asList("1", "2", "3", "4", "5"), new PageRequest(0, 5), 10));

        // then
        assertEquals("1", reader.read());
        assertEquals("2", reader.read());
        assertEquals("3", reader.read());
        assertEquals("4", reader.read());
        assertEquals("5", reader.read());
        assertEquals("페이지 번호가 고정 되어 있으므로 1부터 반복해서 가져옴", "1", reader.read());
        assertEquals("페이지 번호가 고정 되어 있으므로 1부터 반복해서 가져옴", "2", reader.read());
        assertEquals("페이지 번호가 고정 되어 있으므로 1부터 반복해서 가져옴", "3", reader.read());
        assertEquals("페이지 번호가 고정 되어 있으므로 1부터 반복해서 가져옴", "4", reader.read());
        assertEquals("페이지 번호가 고정 되어 있으므로 1부터 반복해서 가져옴", "5", reader.read());
        assertNull("페이지 번호는 고정되어 있지만, 조회된 총 데이터 수 만큼 가져오므로, 더 이상 읽어들일 데이터 없음", reader.read());
    }

    @Test
    public void read_읽어들일_데이터_갯수가_페이지_사이즈_보다_크고_시작_페이지가_1인_경우() {

        // given
        val reader = new FixedPagingItemReader<String>(() ->
                new PageImpl<>(Arrays.asList("1", "2", "3", "4", "5"), new PageRequest(1, 5), 10));

        // then
        assertEquals("1", reader.read());
        assertEquals("2", reader.read());
        assertEquals("3", reader.read());
        assertEquals("4", reader.read());
        assertEquals("5", reader.read());
        assertNull("최초의 총 데이터 수는 10이지만, 시작 페이지 번호가 1이므로, 더 이상 읽어 들일 데이터 없음", reader.read());
    }
}
