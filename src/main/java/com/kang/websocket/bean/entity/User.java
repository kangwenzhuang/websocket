package com.kang.websocket.bean.entity;

public class User{
    private String id;//学生id
    private String name;//学生姓名
    private String course;//所选课程
    private String teacher;//任课老师

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public User(String id, String name, String course, String teacher) {
        this.id = id;
        this.name = name;
        this.course = course;
        this.teacher = teacher;
    }
}