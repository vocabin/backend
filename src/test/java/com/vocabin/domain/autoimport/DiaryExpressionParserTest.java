package com.vocabin.domain.autoimport;

import com.vocabin.domain.upload.ParsedWord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiaryExpressionParserTest {

    @Test
    void 번호가_붙은_표현_목록을_영어와_한국어로_파싱한다() {
        // given
        String expressions = """
                1. go on a date — 데이트하러 가다
                2. meet near City Hall — 시청 근처에서 만나다
                10. feel as if I were in heaven — 천국에 있는 듯한 기분이 들다""";

        // when
        List<ParsedWord> result = DiaryExpressionParser.parse(expressions);

        // then
        assertThat(result).containsExactly(
                new ParsedWord("go on a date", "데이트하러 가다"),
                new ParsedWord("meet near City Hall", "시청 근처에서 만나다"),
                new ParsedWord("feel as if I were in heaven", "천국에 있는 듯한 기분이 들다"));
    }

    @Test
    void 형식이_맞지_않는_줄은_건너뛴다() {
        // given
        String expressions = "오늘의 표현\n1. come across something — 우연히 발견하다\n구분자가 없는 줄\n2. 영어만 있음";

        // when
        List<ParsedWord> result = DiaryExpressionParser.parse(expressions);

        // then
        assertThat(result).containsExactly(new ParsedWord("come across something", "우연히 발견하다"));
    }

    @Test
    void 영어_표현_안의_하이픈은_구분자로_취급하지_않는다() {
        // given
        String expressions = "1. well-known place — 유명한 장소";

        // when
        List<ParsedWord> result = DiaryExpressionParser.parse(expressions);

        // then
        assertThat(result).containsExactly(new ParsedWord("well-known place", "유명한 장소"));
    }

    @Test
    void 비어있거나_null이면_빈_목록을_반환한다() {
        // given & when & then
        assertThat(DiaryExpressionParser.parse(null)).isEmpty();
        assertThat(DiaryExpressionParser.parse("  ")).isEmpty();
    }
}
