package com.mantum.component;

import java.util.List;

public interface OnRequest<T> {

    void success(List<T> value);

    void error(Throwable throwable);
}
