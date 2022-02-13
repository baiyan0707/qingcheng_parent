package com.qingcheng.pojo.system;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Table(name = "tb_admin_role")
public class AdminRole implements Serializable {

    @Id
    private Integer adminId;

    @Id
    private Integer roleId;
}
