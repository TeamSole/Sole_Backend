package com.team6.sole.domain.scrap;

import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.scrap.entity.CourseMemberScrapFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseMemberScrapFolderRepository extends JpaRepository<CourseMemberScrapFolder, Long> {
    void deleteByCourseMemberAndScrapFolder_ScrapFolderId(CourseMember courseMember, Long scrapFolderId);

    List<CourseMemberScrapFolder> findAllByScrapFolder_ScrapFolderId(Long scrapFolderId);
}
