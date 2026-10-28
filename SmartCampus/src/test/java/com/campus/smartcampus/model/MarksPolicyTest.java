package com.campus.smartcampus.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Finding 2: component ceilings 30 / 30 / 50; the course total is the 110-point sum scaled to 100. */
class MarksPolicyTest {

    private static Marks marks(double ca, double mid, double fin) {
        Marks m = new Marks();
        m.setContinuousAssessmentMarks(ca);
        m.setMidtermMarks(mid);
        m.setFinalExamMarks(fin);
        return m;
    }

    @Test
    void fullMarksTotalIsExactly100() {
        assertThat(Marks.MAX_RAW_TOTAL).isEqualTo(110.0);
        assertThat(Marks.MAX_TOTAL).isEqualTo(100.0);
        Marks full = marks(30, 30, 50);
        assertThat(full.getRawTotal()).isEqualTo(110.0);
        assertThat(full.getTotalMarks()).isEqualTo(100.0);
        assertThat(full.getMaxTotal()).isEqualTo(100.0);
    }

    @Test
    void totalNeverExceeds100() {
        Marks m = marks(30, 27.6, 48.1); // raw 105.7 of 110
        assertThat(m.getRawTotal()).isEqualTo(105.7);
        assertThat(m.getTotalMarks()).isEqualTo(96.09);
        assertThat(m.getTotalMarks()).isLessThanOrEqualTo(100.0);
    }

    @Test
    void scalingRoundsToTwoDecimals() {
        assertThat(Marks.toTotalOutOf100(77)).isEqualTo(70.0);
        assertThat(Marks.toTotalOutOf100(55)).isEqualTo(50.0);
        assertThat(Marks.toTotalOutOf100(1)).isEqualTo(0.91);
    }
}
