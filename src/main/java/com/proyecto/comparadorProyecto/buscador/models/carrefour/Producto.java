package com.proyecto.comparadorProyecto.buscador.models.carrefour;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Producto {
    @JsonProperty("display_name")
    private String nombre;
    @JsonProperty("active_price")
    private double precio;
    @JsonProperty("unit_conversion_factor")
    private double tamanoUnidad;
    @JsonProperty("measure_unit")
    private String unidadMedida;
    @JsonProperty("image_path")
    private String imagen;
    @JsonProperty("price_per_unit_text")
    private String precioGranel;
}
