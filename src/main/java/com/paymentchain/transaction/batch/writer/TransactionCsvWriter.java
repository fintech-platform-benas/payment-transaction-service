package com.paymentchain.transaction.batch.writer;

import com.paymentchain.transaction.batch.model.TransactionSummaryDto;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Writer: Escribe reporte CSV con transacciones procesadas.
 *
 * Genera archivo: transaction_report_YYYY-MM-DD.csv
 *
 * @author benas
 */
@Component
public class TransactionCsvWriter {

    @Bean
    public FlatFileItemWriter<TransactionSummaryDto> csvWriter() {

        // Nombre del archivo con fecha
        String filename = "transaction_report_" + LocalDate.now() + ".csv";

        // Configurar extractor de campos (del DTO)
        BeanWrapperFieldExtractor<TransactionSummaryDto> fieldExtractor =
            new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{
            "transactionId",
            "reference",
            "accountIban",
            "date",
            "amount",
            "currency",
            "calculatedFee",
            "totalAmount",
            "status",
            "type",
            "highValue"
        });

        // Configurar agregador de líneas (formato CSV)
        DelimitedLineAggregator<TransactionSummaryDto> lineAggregator =
            new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        // Construir writer
        return new FlatFileItemWriterBuilder<TransactionSummaryDto>()
                .name("transactionCsvWriter")
                .resource(new FileSystemResource("output/" + filename))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> {
                    // Header del CSV
                    writer.write("ID,Reference,IBAN,Date,Amount,Currency,Fee,Total,Status,Type,HighValue");
                })
                .build();
    }
}
