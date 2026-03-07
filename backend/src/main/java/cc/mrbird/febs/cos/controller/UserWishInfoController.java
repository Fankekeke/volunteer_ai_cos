package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.ApplyBillInfo;
import cc.mrbird.febs.cos.entity.UserInfo;
import cc.mrbird.febs.cos.entity.UserWishInfo;
import cc.mrbird.febs.cos.service.IApplyBillInfoService;
import cc.mrbird.febs.cos.service.IUserInfoService;
import cc.mrbird.febs.cos.service.IUserWishInfoService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 志愿填报 控制层
 *
 * @author FanK
 */
@RestController
@RequestMapping("/cos/user-wish-info")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserWishInfoController {

    private final IUserWishInfoService userWishInfoService;

    private final IUserInfoService userInfoService;

    private final IApplyBillInfoService applyBillInfoService;

    /**
     * 分页获取志愿填报信息
     *
     * @param page         分页对象
     * @param userWishInfo 志愿填报信息
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<UserWishInfo> page, UserWishInfo userWishInfo) {
        return R.ok(userWishInfoService.selectUserWishPage(page, userWishInfo));
    }

    /**
     * 查询志愿填报信息
     *
     * @return 志愿填报信息
     */
    @GetMapping("/queryWishTopSchool")
    public R queryWishTopSchool() {
        List<LinkedHashMap<String, Object>> topSchools = userWishInfoService.queryWishTopSchool(10);
        return R.ok(topSchools);
    }

    /**
     * 查询志愿填报信息详情
     *
     * @param id 主键ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(userWishInfoService.getById(id));
    }

    /**
     * 查询志愿填报信息列表
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(userWishInfoService.list());
    }

    /**
     * 新增志愿填报信息
     *
     * @param userWishInfo 志愿填报信息
     * @return 结果
     */
    @PostMapping
    public R save(UserWishInfo userWishInfo) throws FebsException {
        // 获取学生信息
        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, userWishInfo.getUserId()));
        if (userInfo != null) {
            userWishInfo.setUserId(userInfo.getId());
        }
        List<UserWishInfo> userWishInfos = userWishInfoService.list(Wrappers.<UserWishInfo>lambdaQuery().eq(UserWishInfo::getUserId, userInfo.getId()));
        if (userWishInfos.size() >= 3) {
            throw new FebsException("最多填报3个志愿");
        }
        if (CollectionUtil.isNotEmpty(userWishInfos) && userWishInfos.stream().anyMatch(wishInfo -> wishInfo.getIndexNo().equals(userWishInfo.getIndexNo()))) {
            throw new FebsException("志愿排名已绑定");
        }
        if ("1".equals(userWishInfo.getStatus())) {
            ApplyBillInfo applyBillInfo = new ApplyBillInfo();
            applyBillInfo.setUserId(userWishInfo.getUserId());
            applyBillInfo.setStatus("1");
            applyBillInfo.setSchoolId(userWishInfo.getSchoolId());
            // 申请单编号
            applyBillInfo.setCode("BILL-" + System.currentTimeMillis());
            // 创建时间
            applyBillInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
            applyBillInfo.setIndexNo(userWishInfo.getIndexNo());
            applyBillInfo.setDisciplineId(userWishInfo.getDisciplineId());
            applyBillInfoService.save(applyBillInfo);
        }
        return R.ok(userWishInfoService.save(userWishInfo));
    }

    /**
     * 修改志愿填报信息
     *
     * @param userWishInfo 志愿填报信息
     * @return 结果
     */
    @PutMapping
    public R edit(UserWishInfo userWishInfo) throws FebsException {
        UserWishInfo userWishInfoFix = userWishInfoService.getById(userWishInfo.getId());

        List<UserWishInfo> userWishInfos = userWishInfoService.list(Wrappers.<UserWishInfo>lambdaQuery().eq(UserWishInfo::getUserId, userWishInfoFix.getUserId()));
        if (userWishInfos.size() > 3) {
            throw new FebsException("最多填报3个志愿");
        }
        if (CollectionUtil.isNotEmpty(userWishInfos) && userWishInfos.stream().anyMatch(wishInfo -> !wishInfo.getId().equals(userWishInfo.getId()) && wishInfo.getIndexNo().equals(userWishInfo.getIndexNo()))) {
            throw new FebsException("志愿排名已绑定");
        }
        if ("1".equals(userWishInfo.getStatus())) {
            ApplyBillInfo applyBillInfo = new ApplyBillInfo();
            applyBillInfo.setUserId(userWishInfoFix.getUserId());
            applyBillInfo.setStatus("1");
            applyBillInfo.setSchoolId(userWishInfo.getSchoolId());
            // 申请单编号
            applyBillInfo.setCode("BILL-" + System.currentTimeMillis());
            // 创建时间
            applyBillInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
            applyBillInfo.setIndexNo(userWishInfo.getIndexNo());
            applyBillInfo.setDisciplineId(userWishInfo.getDisciplineId());
            applyBillInfoService.save(applyBillInfo);
        }
        return R.ok(userWishInfoService.updateById(userWishInfo));
    }

    /**
     * 删除志愿填报信息
     *
     * @param ids ids
     * @return 志愿填报信息
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(userWishInfoService.removeByIds(ids));
    }
}
