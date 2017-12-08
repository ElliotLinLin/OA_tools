package com.hst.mininurse.utils;

import java.util.ArrayList;

/**
 * Author:lsh
 * Version: 1.0
 * Description:
 * Date: 2017/8/29
 */


//最外层对象<configuration>
public class Config {
    public ConfigSections configSections;
    public DynamicControl DynamicControl;

    public class ControlGroup {
        public String Title;
        public String Content1;
        public String ButtonText1;
        public String Content2;
        public String ButtonText2;
        public String Checked;
        public Controls Controls;
    }

    public class Controls {
        public Control Control;
    }

    public class Control {
        public String ControlType;
        public String Name;
        public String Value;
        public String Data;
        public String Enable;
        public String Checked;

    }
    //第二层第一个儿子的儿子
    public class Section {
        public String name;
        public String type;
    }

    // 第二层第一个儿子 <configSections>
    public class ConfigSections {
        public Section section;
    }
    // 第二层第二个儿子 <DynamicControl
    public class DynamicControl {
        public String CheckSuccessText;
        public String NumberPerPage;
        public String IsMultiCheck;
        public ArrayList<ControlGroup> ControlGroups;
    }
}
