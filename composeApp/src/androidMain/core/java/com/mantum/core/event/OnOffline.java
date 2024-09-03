package com.mantum.core.event;

import com.mantum.core.Mantum;

@Deprecated
public interface OnOffline {

    Mantum.Response onRequest(String... params);
}