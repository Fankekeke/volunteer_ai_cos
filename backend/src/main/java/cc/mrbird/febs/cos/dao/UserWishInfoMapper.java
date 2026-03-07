package cc.mrbird.febs.cos.dao;

import cc.mrbird.febs.cos.entity.UserWishInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 志愿填报 mapper层
 *
 * @author FanK
 */
public interface UserWishInfoMapper extends BaseMapper<UserWishInfo> {

    /**
     * 分页获取志愿填报信息
     *
     * @param page         分页对象
     * @param userWishInfo 志愿填报信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> selectUserWishPage(Page<UserWishInfo> page, @Param("userWishInfo") UserWishInfo userWishInfo);

    /**
     * 查询热门院校统计（按志愿填报数量降序排列）
     *
     * @param limit 限制返回数量，null 则返回全部
     * @return 热门院校列表（包含学校 ID、学校名称、填报次数）
     */
    List<LinkedHashMap<String, Object>> queryWishTopSchool(@Param("limit") Integer limit);
}
