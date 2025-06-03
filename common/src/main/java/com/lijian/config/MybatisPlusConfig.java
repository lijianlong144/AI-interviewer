// common/src/main/java/com/lijian/config/MybatisPlusConfig.java
package com.lijian.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import java.time.LocalDateTime;

/**
 * MyBatis-Plus配置类
 * 
 * @author lijian
 * @since 1.0.0
 */
@Configuration
@EnableTransactionManagement
public class MybatisPlusConfig {

    /**
     * 配置MybatisPlus拦截器
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {


        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();


        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());


        // 如果配置多个插件, 切记分页最后添加PaginationInnerInterceptor
// 分页插件 - 注意这里的导入
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        return interceptor;
//        return new MybatisPlusInterceptor();
    }
    
    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 创建时间和更新时间自动填充
                this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
                // 默认删除状态为0(未删除)
                this.strictInsertFill(metaObject, "isDeleted", () -> 0, Integer.class);
            }
            
            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时间自动填充
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
            }
        };
    }
}