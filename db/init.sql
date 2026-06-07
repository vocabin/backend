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

CREATE TABLE IF NOT EXISTS auto_import_config (
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    member_id    BIGINT   NOT NULL,
    day_of_week  TINYINT  NOT NULL DEFAULT 3 COMMENT '1=Mon 7=Sun',
    hour         TINYINT  NOT NULL DEFAULT 21 COMMENT '0-23',
    enabled      BOOLEAN  NOT NULL DEFAULT TRUE,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_aic_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE CASCADE,
    CONSTRAINT uq_aic_member UNIQUE (member_id)
);

CREATE TABLE IF NOT EXISTS imported_class (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    member_id           BIGINT       NOT NULL,
    external_class_id   VARCHAR(100) NOT NULL,
    word_set_id         BIGINT       NOT NULL,
    imported_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_ic_member   FOREIGN KEY (member_id)   REFERENCES member   (id) ON DELETE CASCADE,
    CONSTRAINT fk_ic_word_set FOREIGN KEY (word_set_id) REFERENCES word_set (id) ON DELETE CASCADE,
    CONSTRAINT uq_ic_member_class UNIQUE (member_id, external_class_id)
);
