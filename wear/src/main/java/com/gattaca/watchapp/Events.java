package com.gattaca.watchapp;

import android.view.View;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by epentangelo on 9/8/14.
 */
public class Events {

    /*
 * Creates a subject that emits events for each click on view
 */
    public static Observable<Object> click(View view) {
        final PublishSubject<Object> subject = PublishSubject.create();
        view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                subject.onNext(new Object());
            }
        });
        return subject;
    }

}
