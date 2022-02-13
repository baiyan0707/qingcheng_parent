package com.qingcheng.pojo.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Table(name = "tb_role_resource")
public class RoleResource implements Serializable {

    @Id
    private Integer role_id;
    @Id
    private Integer resource_id;
}
