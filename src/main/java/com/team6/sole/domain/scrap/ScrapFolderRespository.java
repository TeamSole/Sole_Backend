package com.team6.sole.domain.scrap;

import com.team6.sole.domain.scrap.entity.ScrapFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScrapFolderRespository extends JpaRepository<ScrapFolder, Long> {
    List<ScrapFolder> findByMember_SocialId(String socialId);
}
