package com.qianyitian.badword.rest;

import com.qianyitian.badword.dto.WordFilterResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wordfilter.dfa.Sensitives;

import java.util.Optional;
import java.util.Set;

import org.springframework.web.bind.annotation.*;


@RestController
public class CheckController {
    private Logger logger = LoggerFactory.getLogger(getClass());


     Sensitives sens = null;

    public CheckController() {
        final String dicHome = "./config/dics";
         sens = Sensitives.singleton(dicHome);
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public WordFilterResponseVO check(@RequestBody String sentence) {

        Set<String> sensitives = sens.judge("all", sentence);
        if (sensitives.isEmpty()){ return new WordFilterResponseVO(true);}
        else{ return new WordFilterResponseVO(sensitives);}

    }

    @RequestMapping(value = "/filter", method = RequestMethod.POST)
    public WordFilterResponseVO filter(@RequestBody String sentence) {

        Optional<String> mosaic = sens.mosaic("all", sentence);
        if (!mosaic.isPresent()) {return new WordFilterResponseVO(true);}
        else {return new WordFilterResponseVO(mosaic.get());}

    }
}
