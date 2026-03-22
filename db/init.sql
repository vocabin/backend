CREATE TABLE IF NOT EXISTS word_set (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS word (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    word_set_id BIGINT       NOT NULL,
    english     VARCHAR(500) NOT NULL,
    korean      VARCHAR(500) NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_word_word_set FOREIGN KEY (word_set_id) REFERENCES word_set (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS study_record (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    word_id    BIGINT      NOT NULL,
    mode       VARCHAR(50) NOT NULL COMMENT 'flashcard | speedrun | weak',
    is_correct BOOLEAN     NOT NULL,
    studied_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_study_record_word FOREIGN KEY (word_id) REFERENCES word (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review_schedule (
    id             BIGINT  NOT NULL AUTO_INCREMENT,
    word_id        BIGINT  NOT NULL,
    next_review_at DATE    NOT NULL,
    interval_days  INT     NOT NULL DEFAULT 1,
    ease_factor    FLOAT   NOT NULL DEFAULT 2.5,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_review_schedule_word FOREIGN KEY (word_id) REFERENCES word (id) ON DELETE CASCADE,
    CONSTRAINT uq_review_schedule_word UNIQUE (word_id)
);
