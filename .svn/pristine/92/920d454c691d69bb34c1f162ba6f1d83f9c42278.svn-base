package com.hst.mininurse.utils;

import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Author:lsh
 * Version: 1.0
 * Description:
 * Date: 2017/8/29
 */

public class XmlParseUtil {

    public static String xmlToBean(InputStream is) {
        //XmlPullParserFactory首先是获取实例
        XmlPullParserFactory factory = null;
        Config config = new Config();
        try {

            factory = XmlPullParserFactory.newInstance();
            //利用实例调用setinput将数据写进去
            XmlPullParser parser = factory.newPullParser();
//            InputStream is = new ByteArrayInputStream(content.getBytes());
            parser.setInput(is,"utf-8");
            //通过调用此方法得到当前解析事件
            int type = parser.getEventType();
            Config.DynamicControl dynamicControl = config.new DynamicControl();
            ArrayList<Config.ControlGroup> controlGroupss = new ArrayList<>();
            Config.ConfigSections ConfigSections = null;
            Config.ControlGroup controlGroup = null;
            Config.Controls controls = null;
            Config.Control control = null;
            while (type != XmlPullParser.END_DOCUMENT) {
                switch (type) {
                    case XmlPullParser.START_TAG:
                        if ("configuration".equals(parser.getName())) {
//                             config = new Config();
                        } else if ("configSections".equals(parser.getName())) {
                            ConfigSections = config.new ConfigSections();
                        } else if ("section".equals(parser.getName())) {
                            Config.Section section = config.new Section();
                            String name = parser.getAttributeValue(null, "name");
                            String typeName = parser.getAttributeValue(null, "type");
                            section.name = name;
                            section.type = typeName;
                            ConfigSections.section = section;
                        } else if ("DynamicControl".equals(parser.getName())) {
                            dynamicControl.CheckSuccessText = parser.getAttributeValue(null, "CheckSuccessText");
                            dynamicControl.NumberPerPage = parser.getAttributeValue(null, "NumberPerPage");
                            dynamicControl.IsMultiCheck = parser.getAttributeValue(null, "IsMultiCheck");
                        } else if ("ControlGroup".equals(parser.getName())) {
                            controlGroup = config.new ControlGroup();
                            controlGroup.Title = parser.getAttributeValue(null, "Title");
                            controlGroup.Content1 = parser.getAttributeValue(null, "Content1");
                            controlGroup.ButtonText1 = parser.getAttributeValue(null, "ButtonText1");
                            controlGroup.Content2 = parser.getAttributeValue(null, "Content2");
                            controlGroup.ButtonText2 = parser.getAttributeValue(null, "ButtonText2");
                            controlGroup.Checked = parser.getAttributeValue(null, "Checked");
//                            controlGroup.controls = controls;

                        } else if ("Controls".equals(parser.getName())) {
                            controls = config.new Controls();
                        } else if ("Control".equals(parser.getName())) {
                            control = config.new Control();
                            control.ControlType = parser.getAttributeValue(null, "ControlType");
                            control.Name = parser.getAttributeValue(null, "Name");
                            control.Value = parser.getAttributeValue(null, "Value");
                            control.Data = parser.getAttributeValue(null, "Data");
                            control.Enable = parser.getAttributeValue(null, "Enable");
                            control.Checked = parser.getAttributeValue(null, "Checked");
                            controls.Control = control;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("configSections".equals(parser.getName())) {
                            config.configSections = ConfigSections;
                        } else if ("DynamicControl".equals(parser.getName())) {
                            dynamicControl.ControlGroups = controlGroupss;
                            config.DynamicControl = dynamicControl;
                        } else if ("ControlGroup".equals(parser.getName())) {
                            controlGroup.Controls = controls;
                            controlGroupss.add(controlGroup);
                        } else if ("Controls".equals(parser.getName())) {
                            controls.Control = control;
                        } else if ("configuration".equals(parser.getName())) {
                        }
                        break;
                }
                type = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
//            Toast.makeText(this, "IOException", Toast.LENGTH_SHORT).show();
        }
        String s = new Gson().toJson(config);
        return s;
    }
}
