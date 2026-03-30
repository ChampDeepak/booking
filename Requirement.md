# 🎬 Movie Ticket Booking System — Requirements

## 1. User APIs
- Book movie tickets (provide show ID and seats)
- View theaters in a city
- View movies playing in a city
- View shows for a selected movie
- View seat map for a selected show
- Make payment for selected seats
- Cancel bookings and process refunds

---

## 2. Admin APIs
- Add movies  
- Add theaters  
- Add shows  

---

## 3. User Flow

### Entry Point
- User enters city

### Option 1: Browse by Movies
- View list of movies  
- Select a movie  
- View shows  
- Select a show  

### Option 2: Browse by Theaters
- View list of theaters  
- Select a theater  
- View movies and shows  

### Final Steps
- View seat map  
- Select seats  
- Make payment (UPI, card, net banking)

---

## 4. Seat Booking Logic
- Seats are **temporarily held** when selected  
- Hold has a **time limit**
- If payment is not completed within the hold duration:
  - Seats are automatically released

---

## 5. Pricing
- Base price based on seat categories (e.g., gold, diamond)
- Dynamic pricing factors:
  - Show timing  
  - Day of week  
  - Week of month  
  - Demand  
- Pricing rules should be **configurable**

---

## 6. Theater Structure
- A theater can have **multiple screens**
- Each screen can have **multiple shows**

---

## 7. Movie Details
- Support **multilingual movies**

---

## 8. Cancellation & Refunds
- Users can cancel bookings  
- Refunds are processed via the **original payment method**

---

## 9. User Identification
- Email address is used as a **unique identifier**

---

## 10. Out of Scope
- No internal currency or tokens  
- No discount policies or coupon codes  
- No add-ons  

---

## 11. Design Considerations
- Support **on-demand pricing changes** using configurable rules  
- Handle **concurrent bookings** and avoid race conditions  
- Ensure **scalability** for multiple users and theaters  