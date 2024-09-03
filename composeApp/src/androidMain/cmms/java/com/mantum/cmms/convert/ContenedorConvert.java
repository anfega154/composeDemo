package com.mantum.cmms.convert;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mantum.cmms.entity.Contenedor;
import com.mantum.cmms.entity.EquipmentGrade;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

public class ContenedorConvert implements JsonDeserializer<Contenedor.Response> {

    @Override
    public Contenedor.Response deserialize(
            @NonNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject rootJsonObject = json.getAsJsonObject();
        JsonArray jsonArray = rootJsonObject.getAsJsonArray("sListaEq");

        Contenedor.Response response = new Contenedor.Response();
        List<Contenedor> contenedores = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            Contenedor contenedor = new Contenedor();
            contenedor.setId(jsonObject.get("id").getAsLong());
            contenedor.setCodigo(jsonObject.get("codigo") != null
                    ? jsonObject.get("codigo").getAsString()
                    : null);
            contenedor.setNombre(jsonObject.get("nombre") != null
                    ? jsonObject.get("nombre").getAsString()
                    : null);
            contenedor.setUbicacion(jsonObject.get("ubicacion") != null
                    ? jsonObject.get("ubicacion").getAsString()
                    : null);
            contenedor.setLineanaviera(jsonObject.get("lineanaviera") != null
                    ? jsonObject.get("lineanaviera").getAsString()
                    : null);
            contenedor.setEquipmentgrade(jsonObject.get("equipmentgrade") != null
                    ? jsonObject.get("equipmentgrade").getAsString()
                    : null);

            contenedor.setIdclasificacion(jsonObject.get("idclasificacion") != null
                    ? jsonObject.get("idclasificacion").getAsLong()
                    : null);

            contenedor.setPti(jsonObject.get("pti") != null && jsonObject.get("pti").getAsBoolean());
            contenedor.setEir(jsonObject.get("eir") != null && jsonObject.get("eir").getAsBoolean());

            contenedor.setEstado(jsonObject.get("estado") != null
                    ? jsonObject.get("estado").getAsString()
                    : null);

            contenedor.setPersonalprogramacioneir(jsonObject.get("personalprogramacioneir") != null
                    ? jsonObject.get("personalprogramacioneir").getAsString()
                    : null);

            contenedor.setPersonalprogramacionpti(jsonObject.get("personalprogramacionpti") != null
                    ? jsonObject.get("personalprogramacionpti").getAsString()
                    : null);

            contenedor.setFechaultimaprogramacioneir(jsonObject.get("fechaultimaprogramacioneir") != null
                    ? jsonObject.get("fechaultimaprogramacioneir").getAsString()
                    : null);

            contenedor.setFechaultimaprogramacionpti(jsonObject.get("fechaultimaprogramacionpti") != null
                    ? jsonObject.get("fechaultimaprogramacionpti").getAsString()
                    : null);

            contenedor.setFechaultimaejecucionpti(jsonObject.get("fechaultimaejecucionpti") != null
                    ? jsonObject.get("fechaultimaejecucionpti").getAsString()
                    : null);

            contenedor.setFechaultimaejecucioneir(jsonObject.get("fechaultimaejecucioneir") != null
                    ? jsonObject.get("fechaultimaejecucioneir").getAsString()
                    : null);

            contenedor.setPersonalultimaejecucioneir(jsonObject.get("personalultimaejecucioneir") != null
                    ? jsonObject.get("personalultimaejecucioneir").getAsString()
                    : null);

            contenedor.setPersonalultimaejecucionpti(jsonObject.get("personalultimaejecucionpti") != null
                    ? jsonObject.get("personalultimaejecucionpti").getAsString()
                    : null);

            contenedor.setIdtipo(jsonObject.get("idtipo") != null
                    ? jsonObject.get("idtipo").getAsLong()
                    : null);

            contenedor.setTipo(jsonObject.get("tipo") != null
                    ? jsonObject.get("tipo").getAsString()
                    : null);

            JsonArray jsonArrayEquipmentGrade = jsonObject.get("equipmentgradevalidos") != null
                    ? jsonObject.get("equipmentgradevalidos").getAsJsonArray()
                    : null;


            RealmList<EquipmentGrade> equipmentsGrades = new RealmList<>();
            if (jsonArrayEquipmentGrade != null) {
                for (JsonElement jsonEquipmentGrade : jsonArrayEquipmentGrade) {
                    JsonObject jsonObjectEquipmentGrade = jsonEquipmentGrade.getAsJsonObject();

                    EquipmentGrade equipmentGrade = new EquipmentGrade();
                    equipmentGrade.setId(jsonObjectEquipmentGrade.get("id").getAsLong());
                    equipmentGrade.setName(jsonObjectEquipmentGrade.get("clasificacion") != null
                            ? jsonObjectEquipmentGrade.get("clasificacion").getAsString()
                            : null);

                    equipmentsGrades.add(equipmentGrade);
                }
            }
            contenedor.setEquipmentgradevalidos(equipmentsGrades);

            contenedor.setIdmodelo(jsonObject.get("idmodelo") != null
                    ? jsonObject.get("idmodelo").getAsLong()
                    : null);

            contenedor.setModelo(jsonObject.get("modelo") != null
                    ? jsonObject.get("modelo").getAsString()
                    : null);

            contenedor.setIdmarca(jsonObject.get("idmarca") != null
                    ? jsonObject.get("idmarca").getAsLong()
                    : null);

            contenedor.setMarca(jsonObject.get("marca") != null
                    ? jsonObject.get("marca").getAsString()
                    : null);

            contenedor.setSerial(jsonObject.get("serial") != null
                    ? jsonObject.get("serial").getAsString()
                    : null);

            contenedor.setSoftware(jsonObject.get("software") != null
                    ? jsonObject.get("software").getAsString()
                    : null);

            if (jsonObject.get("detalleinspeccion") != null && jsonObject.get("detalleinspeccion").isJsonObject()) {
                contenedor.setDetalleinspeccion(!jsonObject.get("detalleinspeccion").toString().equals("{}")
                        ? jsonObject.get("detalleinspeccion").toString()
                        : null);
            }
            contenedores.add(contenedor);
        }

        response.setBody(contenedores);
        response.setNext(rootJsonObject.get("iNextPage") != null
                ? rootJsonObject.get("iNextPage").getAsInt()
                : null);
        response.setPercent(rootJsonObject.get("iPercent") != null
                ? rootJsonObject.get("iPercent").getAsInt()
                : null);

        return response;
    }
}
