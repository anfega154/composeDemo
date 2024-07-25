package Komino.JsonToKomino

object BasicKomino {

    fun basicKomino(): String {
        return """
        {
            "fields": [
                {
                    "type": "text",
                    "label": "Código",
                    "placeholder": "Ingresa el código"
                },
                {
                    "type": "text",
                    "label": "Nombre",
                    "placeholder": "Ingresa el nombre"
                },
                {
                    "type": "text",
                    "label": "Instalación de Proceso",
                    "placeholder": "Ingresa la instalación de proceso"
                },
                {
                    "type": "text",
                    "label": "Tamaño",
                    "placeholder": "Ingresa el tamaño"
                },
                {
                    "type": "dropdown",
                    "label": "Estado",
                    "options": ["Activo", "Inactivo"]
                },
                {
                    "type": "textarea",
                    "label": "Observaciones"
                }
            ]
        }
        """
    }
}