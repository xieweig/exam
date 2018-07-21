package org.grow.exam.infrastruture;

import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Author : xieweig
 * Date : 18-7-21
 * <p>
 * Description:
 */
@Service
public class RandomString {

    public Random random = new Random();

    public String random5String(String prefix){
        return prefix + (random.nextInt(90000)+10000);
    }
}
