package com.uzykj.smsSystem.core.controller.module;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uzykj.smsSystem.core.controller.BaseController;
import com.uzykj.smsSystem.core.domain.SmsAccount;
import com.uzykj.smsSystem.core.domain.SysLink;
import com.uzykj.smsSystem.core.domain.SysUser;
import com.uzykj.smsSystem.core.domain.dto.PageDto;
import com.uzykj.smsSystem.core.service.SysLinkService;
import com.uzykj.smsSystem.core.common.json.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @author ghostxbh
 * @date 2020/8/11
 * @description
 */
@Controller
@RequestMapping("/link")
public class LinkController extends BaseController {
    @Autowired
    private SysLinkService sysLinkService;

    @GetMapping("/addPage")
    public String addPage(@RequestParam(value = "linkId", required = false) Integer linkId,
                          Model model, HttpSession session) {
        if (linkId != null) {
            SysLink sysLink = sysLinkService.get(linkId);
            model.addAttribute("link", sysLink);
        }
        return "link/add";
    }

    @PostMapping("/add")
    @ResponseBody
    public JsonResult<SysLink> add(@RequestBody SysLink sysLink, HttpSession session) {
        try {
            SysUser user = checkUser(session);
            sysLink.setCreator(user.getId());
            sysLinkService.add(sysLink);
            return new JsonResult<SysLink>(sysLink);
        } catch (Exception e) {
            log.error("add SysLink error", e);
            return JsonResult.toError("添加短息服务链接失败");
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public JsonResult<SysLink> update(@RequestBody SysLink sysLink) {
        try {
            sysLinkService.update(sysLink);
            return new JsonResult<SysLink>(sysLink);
        } catch (Exception e) {
            log.error("server update error,", e);
            return JsonResult.toError("更新短息服务链接失败");
        }
    }

    @DeleteMapping("/del/{id}")
    @ResponseBody
    public JsonResult del(@PathVariable int id) {
        try {
            sysLinkService.del(id);
            return new JsonResult();
        } catch (Exception e) {
            log.error("server del error,", e);
            return JsonResult.toError("删除短息服务链接失败");
        }
    }

    @GetMapping("/list")
    public String list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                       @RequestParam(value = "searchName", required = false) String searchName,
                       Model model, HttpSession session) {
        try {
            PageDto pageDto = new PageDto();
            pageDto.setPage(page);
            pageDto.setPageSize(pageSize);

            Page<SysLink> allLink = sysLinkService.getAllLink(pageDto);

            model.addAttribute("page", allLink);
            model.addAttribute("searchName", searchName);
        } catch (Exception e) {
            log.error("查询短息服务链接失败", e);
        }
        return "link/list";
    }
}
