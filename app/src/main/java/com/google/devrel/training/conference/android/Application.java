package com.google.devrel.training.conference.android;

import com.appspot.sachi_test_1224.conference.model.Conference;
import com.google.api.client.util.Lists;

import java.util.ArrayList;

/**
 * Created by sachi on 12/3/2014.
 */
public class Application extends android.app.Application {
    ArrayList<String> confs = Lists.newArrayList();
}
