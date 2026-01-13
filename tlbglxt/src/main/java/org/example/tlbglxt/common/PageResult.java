package org.example.tlbglxt.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 分页响应结果工具类
 *
 * @param <T> 数据类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult<T> {

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否为第一页
     */
    private Boolean isFirst;

    /**
     * 是否为最后一页
     */
    private Boolean isLast;

    /**
     * 构造函数
     */
    public PageResult() {
    }

    /**
     * 构造函数
     */
    public PageResult(Integer current, Integer size, Long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records;
        this.pages = calculatePages(total, size);
        this.hasPrevious = current > 1;
        this.hasNext = current < pages;
        this.isFirst = current == 1;
        this.isLast = current.equals(pages.intValue());
    }

    /**
     * 构造函数（全参数）
     */
    public PageResult(Integer current, Integer size, Long total, Long pages, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.pages = pages;
        this.records = records;
        this.hasPrevious = current > 1;
        this.hasNext = current < pages;
        this.isFirst = current == 1;
        this.isLast = current.equals(pages.intValue());
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(Integer current, Integer size, Long total, List<T> records) {
        return new PageResult<>(current, size, total, records);
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty(Integer current, Integer size) {
        return new PageResult<>(current, size, 0L, List.of());
    }

    /**
     * 创建分页结果（从其他分页对象转换）
     */
    public static <T, R> PageResult<T> of(PageResult<R> source, List<T> records) {
        return new PageResult<>(
                source.getCurrent(),
                source.getSize(),
                source.getTotal(),
                source.getPages(),
                records
        );
    }

    /**
     * 包装为统一响应结果
     */
    public Result<PageResult<T>> toResult() {
        return Result.success(this);
    }

    /**
     * 包装为统一响应结果（自定义消息）
     */
    public Result<PageResult<T>> toResult(String message) {
        return Result.success(message, this);
    }

    /**
     * 计算总页数
     */
    private Long calculatePages(Long total, Integer size) {
        if (total == null || total <= 0 || size == null || size <= 0) {
            return 0L;
        }
        return (total + size - 1) / size;
    }

    /**
     * 获取开始记录索引（从1开始）
     */
    public Long getStartIndex() {
        if (current == null || current <= 0 || size == null || size <= 0) {
            return 0L;
        }
        return (long) (current - 1) * size + 1;
    }

    /**
     * 获取结束记录索引
     */
    public Long getEndIndex() {
        if (current == null || current <= 0 || size == null || size <= 0 || total == null || total <= 0) {
            return 0L;
        }
        long endIndex = (long) current * size;
        return Math.min(endIndex, total);
    }

    /**
     * 判断是否为空页
     */
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }

    /**
     * 获取记录数量
     */
    public int getRecordCount() {
        return records == null ? 0 : records.size();
    }

    // ============================== Getter 和 Setter ==============================

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
        this.pages = calculatePages(total, size);
    }

    public Long getPages() {
        return pages;
    }

    public void setPages(Long pages) {
        this.pages = pages;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Boolean getIsFirst() {
        return isFirst;
    }

    public void setIsFirst(Boolean isFirst) {
        this.isFirst = isFirst;
    }

    public Boolean getIsLast() {
        return isLast;
    }

    public void setIsLast(Boolean isLast) {
        this.isLast = isLast;
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "current=" + current +
                ", size=" + size +
                ", total=" + total +
                ", pages=" + pages +
                ", recordCount=" + getRecordCount() +
                ", hasPrevious=" + hasPrevious +
                ", hasNext=" + hasNext +
                ", isFirst=" + isFirst +
                ", isLast=" + isLast +
                '}';
    }
} 