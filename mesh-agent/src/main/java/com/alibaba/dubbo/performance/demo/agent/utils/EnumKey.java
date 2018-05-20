package com.alibaba.dubbo.performance.demo.agent.utils;

public enum EnumKey {

    S {
        public void getInfo() {
        }

    },
    M {
        public void getInfo() {
        }
    },
    L {
        public void getInfo() {
        }
    };

    public static EnumKey getNext(int id) {
        if(ConstUtil.IDEA_MODE)  return EnumKey.S;
        id=((int) id & 7);
        switch ((int) id) {
            case 0:
                return EnumKey.S;
            case 1:
                return EnumKey.M;
            case 2:
                return EnumKey.L;
            case 3:
                return EnumKey.M;
            case 4:
                return EnumKey.L;
            case 5:
                return EnumKey.M;
            case 6:
                return EnumKey.L;
            default:
                return EnumKey.L;
        }
    }
}
