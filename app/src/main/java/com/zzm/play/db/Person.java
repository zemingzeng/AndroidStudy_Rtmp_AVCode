package com.zzm.play.db;

import com.zzm.play.db.core.DbFiled;
import com.zzm.play.db.core.DbTableName;

@DbTableName(tableName = "tb_person")
public class Person {

    @DbFiled(filedName = "name")
    private String name;
    @DbFiled(filedName = "sex")
    private String sex;
    @DbFiled(filedName = "age")
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Person(String name, String sex, int age) {
        this.name = name;
        this.sex = sex;
        this.age = age;
    }

    public Person(){

    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", age=" + age +
                '}';
    }
}
