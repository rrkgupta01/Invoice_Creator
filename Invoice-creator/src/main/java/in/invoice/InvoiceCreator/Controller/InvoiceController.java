package in.invoice.InvoiceCreator.Controller;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import in.invoice.InvoiceCreator.Entity.InvoiceItem;
import jakarta.servlet.http.HttpServletResponse;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class InvoiceController {

    private List<InvoiceItem> items = new ArrayList<>();
    private String customerName;
    private String dateTime;
    
    

    // Endpoint for the main form page
    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("items", items);
        model.addAttribute("totalAmount", items.stream().mapToDouble(InvoiceItem::getTotal).sum());
        return "invoice_form";
    }

    // Add a new item to the list
    @PostMapping("/add-item")
    public String addItem(@ModelAttribute InvoiceItem item) {
        items.add(item);
        return "redirect:/";
    }

    // Set customer name and save current date-time, then redirect to summary page
    @PostMapping("/set-customer")
    public String setCustomer(@RequestParam(required = false) String name) {
        if (name == null || name.trim().isEmpty()) {
            this.customerName = "Customer"; // Assign default value
        } else {
            this.customerName = name;
        }

        // Capture the current date and time
        this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "redirect:/summary";
    }

    // Show the summary page
    @GetMapping("/summary")
    public String showSummary(Model model) {
        model.addAttribute("items", items);
        model.addAttribute("totalAmount", items.stream().mapToDouble(InvoiceItem::getTotal).sum());
        model.addAttribute("customerName", customerName);
        model.addAttribute("dateTime", dateTime);
        return "invoice_summary";
    }

    // Delete an item by its index
    @GetMapping("/delete-item")
    public String deleteItem(@RequestParam int index) {
        items.remove(index);
        return "redirect:/";
    }

    // Edit an existing item
    @GetMapping("/edit-item")
    public String editItem(@RequestParam int index, Model model) {
        InvoiceItem item = items.get(index);
        model.addAttribute("item", item);
        model.addAttribute("index", index);
        return "edit_item";
    }

    // Update an edited item
    @PostMapping("/update-item")
    public String updateItem(@RequestParam int index, @ModelAttribute InvoiceItem updatedItem) {
        items.set(index, updatedItem);
        return "redirect:/";
    }

    // Download the invoice as a PDF
    @PostMapping("/download-invoice")
    public void downloadInvoice(HttpServletResponse response) {
        try {
            // Set response type and headers for downloading a PDF
            response.setContentType("application/pdf");
         //   response.setHeader("Content-Disposition", "attachment; filename=invoice.pdf");


response.setHeader("Content-Disposition", "attachment; filename=" + name + "_" + dateTime + "_invoice.pdf");


            Document document = new Document();
            PdfWriter.getInstance(document, response.getOutputStream()); // May throw DocumentException
            document.open();

            // Add content to the PDF
            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Gupta Shop", font);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Customer Name: " + customerName));
            document.add(new Paragraph("Date and Time: " + dateTime));
            document.add(new Paragraph(" "));

            for (InvoiceItem item : items) {
                document.add(new Paragraph("Item: " + item.getItemName()));
                document.add(new Paragraph("Quantity: " + item.getQuantity()));
                document.add(new Paragraph("Price: ₹" + item.getPrice()));
                document.add(new Paragraph("Total: ₹" + item.getTotal()));
                document.add(new Paragraph(" "));
            }

            double totalAmount = items.stream().mapToDouble(InvoiceItem::getTotal).sum();
            document.add(new Paragraph("Grand Total: ₹" + totalAmount));

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace(); // Log the exception (better use a logger in real-world applications)
            throw new RuntimeException("Error occurred while generating the PDF document.", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("I/O Error occurred while generating the PDF document.", e);
        }
    }
}
