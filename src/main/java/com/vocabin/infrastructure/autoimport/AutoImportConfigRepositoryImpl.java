package com.vocabin.infrastructure.autoimport;

import com.vocabin.application.port.out.AutoImportConfigRepository;
import com.vocabin.domain.autoimport.AutoImportConfig;
import com.vocabin.infrastructure.member.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AutoImportConfigRepositoryImpl implements AutoImportConfigRepository {

    private final AutoImportConfigJpaRepository jpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<AutoImportConfig> findByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId).map(AutoImportConfigEntity::toModel);
    }

    @Override
    public List<AutoImportConfig> findAllEnabled() {
        return jpaRepository.findAllByEnabledTrue()
                .stream()
                .map(AutoImportConfigEntity::toModel)
                .toList();
    }

    @Override
    public AutoImportConfig save(AutoImportConfig config) {
        var member = memberJpaRepository.findById(config.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + config.getMemberId()));

        AutoImportConfigEntity entity;
        if (config.getId() != null) {
            entity = jpaRepository.findById(config.getId())
                    .orElse(AutoImportConfigEntity.from(config, member));
            // re-create from updated config
            entity = AutoImportConfigEntity.from(config, member);
        } else {
            entity = AutoImportConfigEntity.from(config, member);
        }

        return jpaRepository.save(entity).toModel();
    }
}
