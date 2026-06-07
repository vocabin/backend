package com.vocabin.infrastructure.autoimport;

import com.vocabin.domain.autoimport.ImportedClass;
import com.vocabin.infrastructure.member.MemberEntity;
import com.vocabin.infrastructure.wordset.WordSetEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "imported_class")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImportedClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(nullable = false, length = 100)
    private String externalClassId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_set_id", nullable = false)
    private WordSetEntity wordSet;

    @Column(nullable = false)
    private LocalDateTime importedAt;

    public static ImportedClassEntity from(ImportedClass ic, MemberEntity member, WordSetEntity wordSet) {
        ImportedClassEntity entity = new ImportedClassEntity();
        entity.member = member;
        entity.externalClassId = ic.getExternalClassId();
        entity.wordSet = wordSet;
        entity.importedAt = ic.getImportedAt();
        return entity;
    }

    public ImportedClass toModel() {
        return ImportedClass.builder()
                .id(id)
                .memberId(member.getId())
                .externalClassId(externalClassId)
                .wordSetId(wordSet.getId())
                .importedAt(importedAt)
                .build();
    }
}
