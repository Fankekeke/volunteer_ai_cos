package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.ApplyBillInfo;
import cc.mrbird.febs.cos.entity.UserInfo;
import cc.mrbird.febs.cos.service.IApplyBillInfoService;
import cc.mrbird.febs.cos.service.IUserInfoService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 志愿申请 控制层
 *
 * @author FanK
 */
@RestController
@RequestMapping("/cos/apply-bill-info")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplyBillInfoController {

    private final IApplyBillInfoService applyBillInfoService;

    private final IUserInfoService userInfoService;

    /**
     * 分页获取志愿申请信息
     *
     * @param page          分页对象
     * @param applyBillInfo 志愿申请信息
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<ApplyBillInfo> page, ApplyBillInfo applyBillInfo) {
        return R.ok(applyBillInfoService.selectApplyBillPage(page, applyBillInfo));
    }

    /**
     * 添加志愿申请信息
     *
     * @param applyBillInfo 志愿申请信息
     * @return 结果
     */
    @PostMapping("/addApplyBill")
    public R addApplyBill(ApplyBillInfo applyBillInfo) {
        // 获取学生信息
        UserInfo userInfo = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, applyBillInfo.getUserId()));
        if (null != userInfo) {
            applyBillInfo.setUserId(userInfo.getId());
        }
        return R.ok(applyBillInfoService.addApplyBill(applyBillInfo));
    }

    /**
     * 查询志愿申请信息详情
     *
     * @param id 主键ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(applyBillInfoService.getById(id));
    }

    /**
     * 查询志愿申请信息列表
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(applyBillInfoService.list());
    }

    /**
     * 新增志愿申请信息
     *
     * @param applyBillInfo 志愿申请信息
     * @return 结果
     */
    @PostMapping
    public R save(ApplyBillInfo applyBillInfo) {
        return R.ok(applyBillInfoService.save(applyBillInfo));
    }

    /**
     * 修改志愿申请信息
     *
     * @param applyBillInfo 志愿申请信息
     * @return 结果
     */
    @PutMapping
    public R edit(ApplyBillInfo applyBillInfo) throws FebsException {
        ApplyBillInfo applyBillInfo1 = applyBillInfoService.getById(applyBillInfo.getId());
        List<ApplyBillInfo> list = applyBillInfoService.list(Wrappers.<ApplyBillInfo>lambdaQuery().eq(ApplyBillInfo::getUserId, applyBillInfo1.getUserId()));
        if (list.size() < 3) {
            throw new FebsException("当前学生志愿未全部提交");
        }
        // 验证志愿顺序录取逻辑
        validateVolunteerSequence(list, applyBillInfo1);
        applyBillInfo.setSchoolConfirmDate(DateUtil.formatDateTime(new Date()));
        return R.ok(applyBillInfoService.updateById(applyBillInfo));
    }

    /**
     * 验证志愿顺序录取逻辑
     * 只有前一个志愿录取失败，后一个志愿才能进行审核
     *
     * @param allVolunteers 该学生的所有志愿
     * @param currentVolunteer 当前待审核的志愿
     */
    private void validateVolunteerSequence(List<ApplyBillInfo> allVolunteers, ApplyBillInfo currentVolunteer) throws FebsException {
        Integer currentIndexNo = currentVolunteer.getIndexNo();

        // 如果是第一志愿，可以直接审核
        if (currentIndexNo == null || currentIndexNo == 1) {
            return;
        }

        // 获取前一个志愿
        Optional<ApplyBillInfo> previousVolunteerOpt = allVolunteers.stream()
                .filter(v -> v.getIndexNo() != null && v.getIndexNo() == currentIndexNo - 1)
                .findFirst();

        if (!previousVolunteerOpt.isPresent()) {
            throw new FebsException("前一个志愿不存在，无法进行审核");
        }

        ApplyBillInfo previousVolunteer = previousVolunteerOpt.get();

        // 检查前一个志愿是否已审核
        if (StrUtil.isEmpty(previousVolunteer.getStatus()) || "1".equals(previousVolunteer.getStatus())) {
            throw new FebsException("前一个志愿尚未进行审核，无法审核当前志愿");
        }

        // 如果前一个志愿已录取（状态="3"），则当前志愿不能审核
        if ("3".equals(previousVolunteer.getStatus())) {
            throw new FebsException("前一个志愿已被录取，当前志愿不能进行审核");
        }

        // 只有前一个志愿未录取（状态="2"），当前志愿才能审核
        if (!"2".equals(previousVolunteer.getStatus())) {
            throw new FebsException("前一个志愿状态异常，无法审核当前志愿");
        }
    }


    /**
     * 删除志愿申请信息
     *
     * @param ids ids
     * @return 志愿申请信息
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(applyBillInfoService.removeByIds(ids));
    }
}
