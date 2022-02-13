package com.qingcheng.controller.system;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.pojo.system.AdminRole;
import com.qingcheng.pojo.system.Admins;
import com.qingcheng.service.system.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Reference
    private AdminService adminService;

    @GetMapping("/findAll")
    public List<Admin> findAll(){
        return adminService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Admin> findPage(int page, int size){
        return adminService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Admin> findList(@RequestBody Map<String,Object> searchMap){
        return adminService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Admin> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  adminService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public Admins findById(Integer id){
        return adminService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody Admins admins){
        adminService.add(admins);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Admins admins){
        adminService.update(admins);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(Integer id){
        adminService.delete(id);
        return new Result();
    }

    @PostMapping("/edit")
    public Result edit(@RequestBody Map<String,String> editMap){
        if(editMap.size() == 0 || editMap == null){
            throw new RuntimeException("信息不能为空,请输入您的密码信息");
        }
        adminService.editPassword(editMap);
        return new Result();
    }
}
