package com.mantum.core.event;

import com.mantum.core.Mantum;

@Deprecated
public interface OnRequest {

    /**
     * Método que es llamando antes de realizar la petición HTTP. Si la ejecucion retorna
     * un false este parara la ejecución y no llamara al método onAfterRequest
     *
     * @param params {@link String}
     * @return Verdadero si debe continuar de lo contrario false
     */
    boolean onBeforeRequest(String... params);

    /**
     * Método que es llamando despues de realizar la peticion HTTP
     *
     * @param success {@link Mantum.Success}
     * @param params {@link String}
     * @return Verdadero si debe de continuar de lo contrario false
     */
    boolean onAfterRequest(Mantum.Success success, String... params);
}