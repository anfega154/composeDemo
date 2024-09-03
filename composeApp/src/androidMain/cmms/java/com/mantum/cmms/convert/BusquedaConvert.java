package com.mantum.cmms.convert;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.mantum.cmms.entity.Accion;
import com.mantum.cmms.entity.Actividad;
import com.mantum.cmms.entity.Busqueda;
import com.mantum.cmms.entity.DetalleBusqueda;
import com.mantum.cmms.entity.Equipo;
import com.mantum.cmms.entity.Falla;
import com.mantum.cmms.entity.OrdenTrabajo;
import com.mantum.cmms.entity.UltimaLecturaVariable;
import com.mantum.cmms.entity.Variable;
import com.mantum.cmms.entity.VariableCualitativa;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

public class BusquedaConvert implements JsonDeserializer<Busqueda.Request> {

    @Override
    public Busqueda.Request deserialize(JsonElement json, Type typeOfT,
                                        JsonDeserializationContext context) throws JsonParseException {

        Busqueda.Request request = new Busqueda.Request();
        JsonArray jsonArray = json.getAsJsonObject().getAsJsonArray("entities");

        List<Busqueda> busquedas = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // General
            Busqueda busqueda = new Busqueda();
            busqueda.setId(jsonObject.get("id").getAsLong());
            busqueda.setType(jsonObject.get("type").getAsString());
            busqueda.setCode(jsonObject.get("code") != null
                    ? jsonObject.get("code").getAsString()
                    : null);
            busqueda.setName(jsonObject.get("name") != null
                    ? jsonObject.get("name").getAsString()
                    : null);
            busqueda.setIdejecucion(jsonObject.get("idejecucion") != null
                    ? jsonObject.get("idejecucion").getAsLong()
                    : null);

            // Google map
            busqueda.setGmap(jsonObject.get("gmap") != null
                    ? jsonObject.get("gmap").getAsString()
                    : null);

            // Actions
            RealmList<Accion> temporal = new RealmList<>();
            JsonArray jsonArrayActions = jsonObject.get("actions").getAsJsonArray();
            for (JsonElement jsonArrayAction : jsonArrayActions) {
                Accion accion = new Accion();
                accion.setName(jsonArrayAction.getAsString());
                temporal.add(accion);
            }
            busqueda.setActions(temporal);

            // Variables
            RealmList<Variable> variables = new RealmList<>();
            JsonArray jsonArrayVariables = jsonObject.get("variables") != null
                    ? jsonObject.get("variables").getAsJsonArray()
                    : null;

            if (jsonArrayVariables != null) {
                for (JsonElement jsonArrayVariable : jsonArrayVariables) {
                    JsonObject jsonObjectVariable = jsonArrayVariable.getAsJsonObject();

                    Variable variable = new Variable();
                    variable.setId(jsonObjectVariable.get("id").getAsLong());
                    variable.setEntidad(jsonObjectVariable.get("entidad").getAsString());
                    variable.setCodigo(jsonObjectVariable.get("codigo").getAsString());
                    variable.setNombre(jsonObjectVariable.get("nombre").getAsString());
                    variable.setTipo(jsonObjectVariable.get("tipo").getAsString());

                    int orden = jsonObjectVariable.get("orden") != null ? jsonObjectVariable.get("orden").getAsInt() : 0;
                    variable.setOrden(orden);

                    variable.setRangoinferior(jsonObjectVariable.get("rangoinferior") != null
                            ? jsonObjectVariable.get("rangoinferior").getAsString()
                            : null);
                    variable.setRangosuperior(jsonObjectVariable.get("rangosuperior") != null
                            ? jsonObjectVariable.get("rangosuperior").getAsString()
                            : null);

                    RealmList<VariableCualitativa> variablesCualitativas = new RealmList<>();
                    if (jsonObjectVariable.get("valores") != null) {
                        JsonArray jsonArrayVariablesCualitativas = jsonObjectVariable.get("valores").getAsJsonArray();
                        for (JsonElement cualitativas : jsonArrayVariablesCualitativas) {
                            JsonObject jsonObjectCualitativas = cualitativas.getAsJsonObject();
                            VariableCualitativa variableCualitativa = new VariableCualitativa();
                            variableCualitativa.setId(jsonObjectCualitativas.get("id") != null
                                    ? jsonObjectCualitativas.get("id").getAsLong()
                                    : null);
                            variableCualitativa.setValor(jsonObjectCualitativas.get("valor") != null
                                    ? jsonObjectCualitativas.get("valor").getAsString()
                                    : null);
                            variableCualitativa.setDescripcion(jsonObjectCualitativas.get("descripcion") != null
                                    ? jsonObjectCualitativas.get("descripcion").getAsString()
                                    : null);
                            variablesCualitativas.add(variableCualitativa);
                        }
                    }
                    variable.setValores(variablesCualitativas);

                    if (jsonObjectVariable.get("ultimalectura") != null) {
                        JsonObject jsonObjectUltimaLecturaVariable
                                = jsonObjectVariable.get("ultimalectura").getAsJsonObject();

                        UltimaLecturaVariable ultimaLecturaVariable = new UltimaLecturaVariable();
                        ultimaLecturaVariable.setFecha(jsonObjectUltimaLecturaVariable.get("fecha") != null
                                ? jsonObjectUltimaLecturaVariable.get("fecha").getAsString()
                                : null);
                        ultimaLecturaVariable.setValor(jsonObjectUltimaLecturaVariable.get("valor") != null
                                ? jsonObjectUltimaLecturaVariable.get("valor").getAsString()
                                : null);
                        variable.setUltimalectura(ultimaLecturaVariable);
                    }
                    variables.add(variable);
                }
            }

            busqueda.setVariables(variables);

            JsonArray jsonArrayOrdenTrabajo = jsonObject.get("historico_ot") != null
                    ? jsonObject.get("historico_ot").getAsJsonArray()
                    : null;

            if (jsonArrayOrdenTrabajo != null) {
                Type listType = new TypeToken<RealmList<OrdenTrabajo>>(){}.getType();
                RealmList<OrdenTrabajo> historicoOT = new Gson().fromJson(
                        jsonArrayOrdenTrabajo.toString(), listType);
                busqueda.setHistoricoOT(historicoOT);
            }

            JsonArray jsonArrayFalla = jsonObject.get("fallas") != null
                    ? jsonObject.get("fallas").getAsJsonArray()
                    : null;

            if (jsonArrayFalla != null) {
                Type listType = new TypeToken<RealmList<Falla>>(){}.getType();
                RealmList<Falla> fallas = new Gson().fromJson(
                        jsonArrayFalla.toString(), listType);
                busqueda.setFallas(fallas);
            }

            RealmList<DetalleBusqueda> detalles = new RealmList<>();
            if (jsonObject.get("data") != null && jsonObject.get("data").isJsonArray()) {
                JsonArray jsonArrayData = jsonObject.get("data").getAsJsonArray();
                if (jsonArrayData != null) {
                    for (JsonElement jsonDetalle : jsonArrayData) {
                        JsonObject temp = jsonDetalle.getAsJsonObject();

                        DetalleBusqueda detalle = new DetalleBusqueda();
                        detalle.setTitle(temp.get("title") != null
                                ? temp.get("title").getAsString()
                                : null);
                        detalle.setValue(temp.get("value") != null
                                ? temp.get("value").getAsString()
                                : null);
                        detalles.add(detalle);
                    }
                }
            }

            if (jsonObject.get("detalle") != null && jsonObject.get("detalle").isJsonObject()) {
                JsonObject jsonObjectDetalle = jsonObject.get("detalle").getAsJsonObject();
                if (jsonObjectDetalle != null) {
                    busqueda.setDetalle(jsonObjectDetalle.toString());
                    if (jsonObjectDetalle.get("dataam").isJsonArray()) {
                        JsonArray jsonArrayDataAM = jsonObjectDetalle.getAsJsonArray("dataam");
                        RealmList<Actividad> actividades = new RealmList<>();
                        for (JsonElement element : jsonArrayDataAM) {
                            String referencia = element.getAsJsonObject().toString();
                            Actividad actividad = new Gson().fromJson(referencia, Actividad.class);
                            actividades.add(actividad);
                        }
                        busqueda.setActividades(actividades);
                    }
                }
            }

            // O.T. - S.S. - R.T.
            if ("OT".equals(busqueda.getType()) || "SS".equals(busqueda.getType())  || "RT".equals(busqueda.getType()) ) {
                if (jsonObject.get("data").isJsonObject()) {
                    busqueda.setDetalle(jsonObject.get("data").getAsJsonObject().toString());
                }
            }

            busqueda.setData(detalles);
            busquedas.add(busqueda);
        }

        request.setEntities(busquedas);
        return request;
    }
}
