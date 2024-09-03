package com.mantum.cmms.service;

import android.content.Context;
import androidx.annotation.NonNull;

import com.mantum.component.adapter.CategoryAdapter;
import com.mantum.component.adapter.FormatoAdapter;
import com.mantum.cmms.database.Database;
import com.mantum.cmms.entity.Categoria;
import com.mantum.cmms.entity.Category;
import com.mantum.cmms.entity.Cuenta;
import com.mantum.cmms.entity.Formato;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class FormatoService {

    private final Realm realm;

    public FormatoService(@NonNull Context context) {
        this.realm = new Database(context).instance();
    }

    public List<Formato> getFormatos() {
        List<Formato> formatos = new ArrayList<>();

        Cuenta cuenta = realm.where(Cuenta.class)
        .equalTo("active", true)
        .findFirst();

        if (cuenta == null) {
            return formatos;
        }

        RealmResults<Formato> formatoes = realm.where(Formato.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findAll();

        formatos.addAll(realm.copyFromRealm(formatoes));
        return formatos;
    }


    public List<Category> getCategoriesByFormato(Formato formato, Boolean firma) {
        List<Category> categories = new ArrayList<>();

        Cuenta cuenta = realm.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null || formato.getCategorias() == null) {
            return categories;
        }

        for (int i = 0; i < formato.getCategorias().size(); i++) {
            Category temp = formato.getCategorias().get(i);
            if (temp.getFirma() == firma) {
                categories.add(temp);
            }
        }

        return categories;
    }


    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();

        Cuenta cuenta = realm.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return categories;
        }

        RealmResults<Category> results = realm.where(Category.class)
                .distinct("id")
                .equalTo("firma", true)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findAll();

        categories = realm.copyFromRealm(results);
        return categories;
    }


    public List<FormatoAdapter> getFormatosImagenes() {
        List<FormatoAdapter> formatos = new ArrayList<>();

        Cuenta cuenta = realm.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return formatos;
        }

        RealmResults<Formato> formatoes = realm.where(Formato.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .findAll();

        for (Formato formato : formatoes) {
            FormatoAdapter temp = new FormatoAdapter(formato.getId(), formato.getFormato());

            List<CategoryAdapter> listCategorias = new ArrayList<>();
            for (Category category: formato.getCategorias()) {
                if (!category.getFirma()) {
                    CategoryAdapter tempCat = new CategoryAdapter(category.getId(), category.getNombre(), category.getFirma());
                    listCategorias.add(tempCat);
                }
            }

            if (listCategorias.size() > 0) {
                temp.setCategorias(listCategorias);
                formatos.add(temp);
            }
        }

        return formatos;
    }


    public List<CategoryAdapter> getAllCategoriesImagenes() {
        List<CategoryAdapter> listCategorias = new ArrayList<>();

        Cuenta cuenta = realm.where(Cuenta.class)
                .equalTo("active", true)
                .findFirst();

        if (cuenta == null) {
            return listCategorias;
        }

        RealmResults<Categoria> results = realm.where(Categoria.class)
                .equalTo("cuenta.UUID", cuenta.getUUID())
                .equalTo("tipo", "imagen")
                .findAll();

        for (Categoria category : results) {
            CategoryAdapter temp = new CategoryAdapter(category.getId(), category.getNombre(), false);
            listCategorias.add(temp);
        }

        return listCategorias;
    }

}
