package com.tmall.util;

import org.springframework.data.domain.Page;

import java.util.List;

public class Page4Navigator<T> {
    //jpa传递出来的分页对象，Page4Navigator类就是对它进行封装以达到拓展的效果
    Page<T> pageFromJPA;

    //分页的时候，如果总页数比较多，那么显示出来的分页超链一个有几个。
    // 比如如果分页出来的超链是这样的：[8, 9, 10, 11, 12]，那么navigatePages就是5
    int navigatePages;

    //总页面数
    int totalPages;

    //第几页（基0）
    int number;

    //总共有多少条数据
    long totalElements;

    //一页最多有多少条数据
    int size;

    //当前页有多少跳数据（与size不同的的是，最后一页可能不满size个）
    int numberOfElements;

    //数据集合
    List<T> content;

    //是否有数据
    boolean isHasContent;

    //是否是首页
    boolean first;

    //是否是末叶
    boolean last;

    //是否有下一页
    boolean isHasNext;

    //是否有上一页
    boolean isHasPrevious;

    //分页的时候，如果总页数比较多，那么显示出来的分页超链一个有几个。
    //比如如果分页是这样的：[8,9,10,11,12]，那么navigatepageNums就是这个数组：[8,9,10,11,12]
    int[] navigatepageNums;

    public Page4Navigator(){
        //这个空的分页是为了 Redis 从 json格式转换为 Page4Navigator 对象而专门提供的
    }

    public Page4Navigator(Page<T> pageFromJPA, int navigatePages){
        this.pageFromJPA = pageFromJPA;

        this.navigatePages = navigatePages;

        totalPages = pageFromJPA.getTotalPages();

        number = pageFromJPA.getNumber();

        totalElements = pageFromJPA.getTotalElements();

        size = pageFromJPA.getSize();

        numberOfElements = pageFromJPA.getNumberOfElements();

        content = pageFromJPA.getContent();

        isHasContent = pageFromJPA.hasContent();

        first = pageFromJPA.isFirst();

        last = pageFromJPA.isLast();

        isHasNext = pageFromJPA.hasNext();

        isHasPrevious = pageFromJPA.hasPrevious();

        calcNavigatepageNums();
    }

    private void calcNavigatepageNums(){
        int navigatePageNums[];
        int totalPages = getTotalPages();
        int num = getNumber();

        //当总页数小于或等于导航页码数时
        if(totalPages <= navigatePages){
            navigatePageNums = new int[totalPages];
            for(int i = 0; i < totalPages; i++){
                navigatePageNums[i] = i + 1;
            }
        } else { //当总页数大于导航页码数时
            navigatePageNums = new int[navigatePages];
            int startNum = num - navigatePages / 2;
            int endNum = num + navigatePages / 2;

            if(startNum < 1){
                startNum = 1;
                //最前navigatePages页
                for(int i = 0; i < navigatePages; i++){
                    navigatePageNums[i] = startNum++;
                }
            } else if(endNum > totalPages){
                endNum = totalPages;
                //最后navigatePages页
                for(int i = navigatePages - 1; i >= 0;i--){
                    navigatePageNums[i] = endNum--;
                }
            } else {
                //所有中间页
                for (int i = 0; i < navigatePages; i++){
                    navigatePageNums[i] = startNum++;
                }
            }
        }
        this.navigatepageNums = navigatePageNums;
    }

    public int getNavigatePages() {
        return navigatePages;
    }

    public void setNavigatePages(int navigatePages) {
        this.navigatePages = navigatePages;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public boolean isHasContent() {
        return isHasContent;
    }

    public void setHasContent(boolean hasContent) {
        isHasContent = hasContent;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isHasNext() {
        return isHasNext;
    }

    public void setHasNext(boolean hasNext) {
        isHasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return isHasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        isHasPrevious = hasPrevious;
    }

    public int[] getNavigatepageNums() {
        return navigatepageNums;
    }

    public void setNavigatepageNums(int[] navigatepageNums) {
        this.navigatepageNums = navigatepageNums;
    }
}
