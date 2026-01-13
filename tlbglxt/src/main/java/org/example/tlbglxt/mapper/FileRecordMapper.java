package org.example.tlbglxt.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.tlbglxt.entity.FileRecord;

import java.util.List;

/**
 * 文件记录数据访问接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Mapper
public interface FileRecordMapper {

    /**
     * 插入文件记录
     *
     * @param fileRecord 文件记录
     * @return 影响行数
     */
    int insert(FileRecord fileRecord);

    /**
     * 根据ID查询文件记录
     *
     * @param id 文件ID
     * @return 文件记录
     */
    FileRecord selectById(Long id);

    /**
     * 更新文件记录
     *
     * @param fileRecord 文件记录
     * @return 影响行数
     */
    int updateById(FileRecord fileRecord);

    /**
     * 根据业务ID和类型查询文件列表
     *
     * @param businessId   业务ID
     * @param businessType 业务类型
     * @return 文件记录列表
     */
    List<FileRecord> selectByBusiness(@Param("businessId") Long businessId, 
                                     @Param("businessType") String businessType);

    /**
     * 根据分类和业务ID查询文件列表
     *
     * @param category   文件分类
     * @param businessId 业务ID
     * @return 文件记录列表
     */
    List<FileRecord> selectByCategory(@Param("category") String category, 
                                     @Param("businessId") Long businessId);

    /**
     * 根据用户ID查询文件列表
     *
     * @param userId 用户ID
     * @return 文件记录列表
     */
    List<FileRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 软删除文件记录
     *
     * @param id     文件ID
     * @param userId 操作用户ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 批量软删除文件记录
     *
     * @param ids    文件ID列表
     * @param userId 操作用户ID
     * @return 影响行数
     */
    int batchDeleteByIds(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    /**
     * 查询指定时间之前的文件记录（用于清理）
     *
     * @param days 天数
     * @return 文件记录列表
     */
    List<FileRecord> selectExpiredFiles(@Param("days") int days);
} 