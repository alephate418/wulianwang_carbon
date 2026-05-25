create table campus_area
(
    id          bigint auto_increment comment '区域ID'
        primary key,
    area_name   varchar(50)                           not null comment '区域名称（如：一号教学楼、宿舍区）',
    area_type   varchar(20)                           not null comment '区域类型（教学/宿舍/食堂/绿化/交通）',
    area_size   double                                null comment '面积（㎡）',
    location    varchar(50)                           null comment 'GIS坐标（纬度,经度）',
    status      tinyint     default 1                 null comment '状态（1-启用，0-禁用）',
    create_time datetime    default CURRENT_TIMESTAMP null,
    update_time datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    green_area  double      default 0                 not null comment '绿化面积(㎡)',
    plant_type  varchar(50) default ''                not null comment '植物类型(如:乔木/灌木/草地)'
)
    comment '校园区域信息表';

create table carbon_coefficient
(
    id                bigint auto_increment comment 'ID'
        primary key,
    coefficient_type  varchar(20)                        not null comment '系数类型（电力/燃气/燃油/固碳）',
    coefficient_value double                             not null comment '系数值（kgCO₂/kWh 或 kgCO₂/m³ 等）',
    remark            varchar(100)                       null comment '备注（如：全国平均电力系数）',
    create_time       datetime default CURRENT_TIMESTAMP null,
    update_time       datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_type
        unique (coefficient_type)
)
    comment '碳排放系数配置表';

create table carbon_data
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    area_id       bigint                             not null comment '区域ID',
    total_carbon  double   default 0                 null comment '总碳排放（kgCO₂）',
    sequestration double   default 0                 null comment '固碳量（kgCO₂）',
    net_carbon    double   default 0                 null comment '净碳排放（kgCO₂）',
    collect_time  datetime                           not null comment '采集时间',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    constraint carbon_data_campus_area_id_fk
        foreign key (area_id) references campus_area (id)
            on update cascade on delete cascade
)
    comment '碳排放时序数据表';

create index idx_area_id
    on carbon_data (area_id);

create index idx_area_time
    on carbon_data (area_id, collect_time);

create index idx_collect_time
    on carbon_data (collect_time);

create table carbon_emission_consume
(
    id               bigint auto_increment comment 'ID'
        primary key,
    cd_id            bigint                             not null comment '关联carbon_data的主键',
    coefficient_type varchar(20)                        not null comment '消耗类型（ELECTRICITY/GAS/FUEL/TREE/SHRUB/LAWN）',
    consume_amount   double                             not null comment '消耗量（kWh/m³/L/㎡）',
    carbon_emission  double                             not null comment '碳排放量（kgCO₂，固碳为负数）',
    create_time      datetime default CURRENT_TIMESTAMP null,
    update_time      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint carbon_emission_consume_carbon_coefficient_coefficient_type_fk
        foreign key (coefficient_type) references carbon_coefficient (coefficient_type),
    constraint carbon_emission_consume_carbon_data_id_fk
        foreign key (cd_id) references carbon_data (id)
            on delete cascade
)
    comment '能源消耗与碳排放数据表';

create table carbon_warning
(
    id            bigint auto_increment comment 'ID'
        primary key,
    carbon_id     bigint                             null,
    area_id       bigint                             not null comment '区域ID',
    warning_time  datetime default CURRENT_TIMESTAMP null comment '预警时间',
    carbon_value  double                             not null comment '超标碳排放量（kgCO₂）',
    threshold     double                             not null comment '阈值（kgCO₂）',
    warning_type  varchar(20)                        not null comment '预警类型（电力/燃气/总排放）',
    handle_status tinyint  default 0                 null comment '处理状态（0-未处理，1-已处理）',
    handle_time   datetime                           null comment '处理时间',
    handle_remark varchar(200)                       null comment '处理备注',
    constraint carbon_warning_campus_area_id_fk
        foreign key (area_id) references campus_area (id)
            on update cascade on delete cascade,
    constraint carbon_warning_carbon_data_id_fk
        foreign key (carbon_id) references carbon_data (id)
)
    comment '碳排放预警记录表';

create index idx_area_id
    on carbon_warning (area_id);

create index idx_warning_time
    on carbon_warning (warning_time);


