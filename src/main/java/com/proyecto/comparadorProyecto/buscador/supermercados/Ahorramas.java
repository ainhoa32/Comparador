package com.proyecto.comparadorProyecto.buscador.supermercados;

import com.proyecto.comparadorProyecto.buscador.CalculadorPrioridad;
import com.proyecto.comparadorProyecto.buscador.Supermercado;
import com.proyecto.comparadorProyecto.buscador.ClienteJsoup;
import com.proyecto.comparadorProyecto.dto.ProductoDto;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Ahorramas implements Supermercado {
    private final CalculadorPrioridad calculadorPrioridad;
    private final ClienteJsoup clienteJsoup;

    @Override
    public CompletableFuture<List<ProductoDto>> obtenerListaSupermercado(String producto) {
        String productoCodificado = URLEncoder.encode(producto, StandardCharsets.UTF_8);
        String url = "https://www.ahorramas.com/buscador?q=" + productoCodificado;
        return clienteJsoup.realizarPeticion(url)
                .thenApply(document -> convertirDocumentoALista(document))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return new ArrayList<>();
                });
    }

    public List<ProductoDto> convertirDocumentoALista(Document doc) {
        Elements divProducto = doc.select("div.product");
        Element primerElemento = divProducto.first();

        if (primerElemento == null) {
            return List.of();
        }

        List<String> categoriasPrioritarias = obtenerCategorias(primerElemento);

        return divProducto.stream()
                .map(producto -> mapearProducto(producto, categoriasPrioritarias))
                .limit(10)
                .collect(Collectors.toList());
    }

    public ProductoDto mapearProducto(Element producto, List<String> categoriasPrioritarias) {
        String nombreProducto = producto.select("h2.product-name-gtm").text();
        int index = Integer.parseInt(producto.select("div.product-tile").attr("data-index"));
        Double precio = Double.parseDouble(producto.select("div.add-to-cart").attr("data-price"));
        String [] preciosGranel = producto.select("span.unit-price-per-unit").text().split(" ");
        String precioGranelString = preciosGranel[preciosGranel.length-1];
        Double precioGranel = Double.parseDouble(precioGranelString.substring(0, precioGranelString.indexOf('€')).replace(",", "."));
        String unidadMedida = precioGranelString.substring(precioGranelString.indexOf("/") + 1);
        String unidadMedidaAbrev = unidadMedida.equals("LITRO") ? "l" : "kg";
        String urlImagen = producto.select("img.tile-image").attr("src");
        int prioridad = calculadorPrioridad.calcularSegunCategorias(obtenerCategorias(producto), categoriasPrioritarias);

        double tamanoUnidad = precio/precioGranel;

        return ProductoDto.builder()
                .nombre(nombreProducto)
                .supermercado("AHORRAMAS")
                .unidadMedida(unidadMedidaAbrev)
                .urlImagen(urlImagen)
                .prioridad(prioridad)
                .index(index)
                .precioGranel(precioGranel)
                .precio(precio)
                .tamanoUnidad(tamanoUnidad)
                .build();
    }

    private List<String> obtenerCategorias(Element elemento) {
        return List.of(
                elemento.select("div.product-tile").attr("data-category2"),
                elemento.select("div.product-tile").attr("data-category1")
        );
    }
}
