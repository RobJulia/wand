package com.pcq;

import com.pcq.annotation.WandMethod;
import org.springframework.stereotype.Component;

/**
 * Created by pcq on 2017/5/19.
 */
@Component
public class UserService {

    @WandMethod
    public void query(int a,Long b,String c){

    }
    @WandMethod(params = "c")
    public void update(String c){
        System.out.println("XXXXXXXXXXXXXXX:"+c);

    }
}
