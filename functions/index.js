const functions = require("firebase-functions");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();

// Configuration - Replace with your SMTP details
const transporter = nodemailer.createTransport({
    service: "gmail", // e.g., 'gmail'
    auth: {
        user: "YOUR_KIOSK_EMAIL@gmail.com",
        pass: "YOUR_APP_PASSWORD" // For Gmail, use an 'App Password', not your main password
    }
});

const ORDER_MANAGER_EMAIL = "manager@karima.org"; // The person who manages orders
exports.onNewOrder = functions.firestore
    .document("orderHeader/{orderId}")
    .onCreate(async (snap, context) => {
        const order = snap.data();
        const orderId = context.params.orderId;

        // Only send emails for "Place Order" fulfillment
        if (order.fulfillmentMode !== "PLACE_ORDER") return null;

        // 1. Fetch Order Lines
        const linesSnapshot = await admin.firestore()
            .collection("orderLines")
            .where("orderId", "==", orderId)
            .get();

        let itemsHtml = "<ul>";
        linesSnapshot.forEach(doc => {
            const line = doc.data();
            itemsHtml += `<li>${line.productName} ${line.sizeName ? '('+line.sizeName+')' : ''} x ${line.quantity} - £${line.price}</li>`;
        });
        itemsHtml += "</ul>";
		
		// 2. Email to Customer
        if (order.customerEmail) {
            const customerMailOptions = {
                from: '"Karima Store" <YOUR_KIOSK_EMAIL@gmail.com>',
                to: order.customerEmail,
                subject: `Order Confirmation - #${orderId.substring(0,8)}`,
                html: `
                    <h1>Jazakum Allahu Khayran, ${order.customerName}!</h1>
                    <p>Your order has been placed successfully via our donation kiosk.</p>
                    <h3>Order Summary:</h3>
                    ${itemsHtml}
                    <p>Total: £${order.totalAmount}</p>
                    <p>We will contact you soon regarding your delivery.</p>
                `
            };
            await transporter.sendMail(customerMailOptions);
        }
        // 3. Email to Manager
        const managerMailOptions = {
            from: '"Donation Kiosk" <YOUR_KIOSK_EMAIL@gmail.com>',
            to: ORDER_MANAGER_EMAIL,
            subject: `NEW ORDER: ${order.customerName}`,
            html: `
                <h2>New Order Received</h2>
                <p><strong>Customer:</strong> ${order.customerName}</p>
                <p><strong>Email:</strong> ${order.customerEmail || "Not provided"}</p>
                <p><strong>Items:</strong></p>
                ${itemsHtml}
                <p><strong>Total:</strong> £${order.totalAmount}</p>
                <p>Please log in to the Karima Web Portal to manage this order.</p>
            `
        };
        return transporter.sendMail(managerMailOptions);
    });