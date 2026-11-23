package com.vocabin.domain.listening;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TranscriptSentencesTest {

    private static TranscriptLine line(double start, double duration, String text) {
        return new TranscriptLine(start, duration, text);
    }

    // The caption lines from the podcast screenshot (cut every 1-2 seconds, mid-sentence)
    private static final List<TranscriptLine> PODCAST = List.of(
            line(424.0, 2.0, "But I feel like on the East Coast, maybe"),
            line(426.0, 1.5, "you experience this. Umbrellas were kind"),
            line(427.5, 1.8, "of like a take it or leave it sort of"),
            line(429.3, 1.2, "thing. Like you might take your"),
            line(430.5, 1.0, "umbrella, you might leave it, and then"),
            line(431.5, 1.6, "you might pick up in certain situations"),
            line(433.1, 1.0, "in certain places."));

    @Test
    void 여러_줄에_걸친_문장을_하나로_합친다() {
        // given & when
        List<TranscriptSentence> sentences = TranscriptSentences.from(PODCAST);

        // then
        assertThat(sentences).extracting(TranscriptSentence::text).containsExactly(
                "But I feel like on the East Coast, maybe you experience this.",
                "Umbrellas were kind of like a take it or leave it sort of thing.",
                "Like you might take your umbrella, you might leave it, and then you might pick up in certain situations in certain places.");
    }

    @Test
    void 문장_시작_시간은_그_문장이_시작되는_자막_위치로_계산한다() {
        // given & when
        List<TranscriptSentence> sentences = TranscriptSentences.from(PODCAST);

        // then
        assertThat(sentences.get(0).start()).isEqualTo(424.0);
        // "Umbrellas" begins in the middle of the 426.0s line, so it starts after 426.0
        assertThat(sentences.get(1).start()).isGreaterThan(426.0).isLessThan(427.5);
        // and the sentence ends where "thing." ends inside the 429.3s line
        assertThat(sentences.get(1).end()).isGreaterThan(429.3).isLessThan(430.5);
        assertThat(sentences.get(2).end()).isCloseTo(434.1, within(0.01));
    }

    @Test
    void 약어와_이니셜에서는_문장을_나누지_않는다() {
        // given
        List<TranscriptLine> lines = List.of(line(0, 3, "Mr. Smith moved to the U.S. last year."), line(3, 2, "J. K. Rowling agreed."));

        // when
        List<TranscriptSentence> sentences = TranscriptSentences.from(lines);

        // then
        assertThat(sentences).extracting(TranscriptSentence::text)
                .containsExactly("Mr. Smith moved to the U.S. last year.", "J. K. Rowling agreed.");
    }

    @Test
    void 물음표와_느낌표도_문장_끝으로_본다() {
        // given
        List<TranscriptLine> lines = List.of(line(0, 2, "Did you see that? Wow! That was"), line(2, 2, "kind of crazy."));

        // when & then
        assertThat(TranscriptSentences.from(lines)).extracting(TranscriptSentence::text)
                .containsExactly("Did you see that?", "Wow!", "That was kind of crazy.");
    }

    @Test
    void 구두점이_없는_자동_자막은_말이_멈춘_곳에서_나눈다() {
        // given
        List<TranscriptLine> lines = List.of(
                line(0.0, 2.0, "so we went there"),
                line(2.0, 2.0, "and it was great"),
                line(6.0, 2.0, "then we came back home"));  // 2 second pause before this line

        // when & then
        assertThat(TranscriptSentences.from(lines)).extracting(TranscriptSentence::text)
                .containsExactly("so we went there and it was great", "then we came back home");
    }

    @Test
    void 구두점이_없는_자동_자막은_20단어를_넘기지_않게_나눈다() {
        // given: continuous speech without punctuation, 5 words per line
        List<TranscriptLine> lines = IntStream.range(0, 8)
                .mapToObj(i -> line(i * 2.0, 2.0, "one two three four five"))
                .toList();

        // when
        List<TranscriptSentence> sentences = TranscriptSentences.from(lines);

        // then
        assertThat(sentences).hasSize(2);
        assertThat(sentences).allSatisfy(s -> assertThat(s.text().split(" ")).hasSizeLessThanOrEqualTo(20));
    }

    @Test
    void 자막이_없으면_빈_목록이다() {
        // given & when & then
        assertThat(TranscriptSentences.from(List.of())).isEmpty();
    }
}
