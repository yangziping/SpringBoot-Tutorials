package com.feichaoyu.springbatch.config;

import com.feichaoyu.springbatch.model.User;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

/**
 * @Author feichaoyu
 * @Date 2019/7/31
 */
@Configuration
@EnableBatchProcessing(modular = true) // 多任务需要设置 modular = true
public class SpringBatchConfig {

    @Autowired
    DataSource dataSource;
    @Autowired
    StepBuilderFactory stepBuilderFactory;
    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Bean
    @StepScope
    FlatFileItemReader<User> itemReader() {
        FlatFileItemReader<User> reader = new FlatFileItemReader<>();
        // 文件的第一行是标题，跳过
        reader.setLinesToSkip(1);
        reader.setResource(new ClassPathResource("data.txt"));
        // 外层花括号是匿名内部类的写法，内层花括号在初始化对象时调用，类似于构造器
        reader.setLineMapper(new DefaultLineMapper<User>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                // 设置每一行的数据信息
                setNames("name", "age", "address");
                // 配置列与列之间的间隔符
                setDelimiter(" ");
            }});
            // 设置要映射的实体类属性
            setFieldSetMapper(new BeanWrapperFieldSetMapper<User>() {{
                setTargetType(User.class);
            }});
        }});
        return reader;
    }

    @Bean
    JdbcBatchItemWriter<User> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<User> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        // 配置数据以及数据插入SQL，注意占位符的写法是 ":属性名"
        writer.setSql("insert into user(name, age, address)" + "values(:name, :age, :address)");
        // 将实体类的属性和SQL中的占位符一一映射
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        return writer;
    }

    @Bean
    Job job() {
        // 通过jobBuilderFactory构建一个Job，get方法的参数就是Job的name
        return jobBuilderFactory.get("job")
                .start(step())
                .build();
    }

    @Bean
    Step step() {
        // 通过stepBuilderFactory构建一个Step，get方法的参数就是该Step的name
        return stepBuilderFactory.get("step")
                // 参数2表示每读取到两条数据就执行一次write操作
                .<User, User>chunk(2)
                .reader(itemReader())
                .writer(jdbcBatchItemWriter())
                .build();
    }

}
