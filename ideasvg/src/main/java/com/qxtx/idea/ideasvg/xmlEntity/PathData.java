package com.qxtx.idea.ideasvg.xmlEntity;

import java.util.Arrays;
import java.util.List;

/**
 * Create date 2020/10/6 22:09
 * @author QXTX-WIN
 * Description 路径指令符数据实体类
 */
public class PathData {

    /** 路径指令符 */
    private char command;

    /** 路径数据列表 */
    private final List<Float> valueList;

    /** 路径终点坐标 */
    private final float[] endCoordinate;

    /**
     * @param command 指令符
     * @param valueList 数值列表
     * @param endX 终点x坐标
     * @param endY 终点y坐标
     */
    public PathData(char command, List<Float> valueList, float endX, float endY) {
        this.command = command;
        this.valueList = valueList;
        endCoordinate = new float[] {endX, endY};
    }

    public char getCommand() {
        return command;
    }

    public void setCommand(char command) {
        this.command = command;
    }

    public List<Float> getValueList() {
        return valueList;
    }

    public float[] getEndCoordinate() {
        return endCoordinate;
    }

    @Override
    public String toString() {
        return command + Arrays.toString(valueList.toArray(new Float[0]));
    }
}
