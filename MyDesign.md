1. I will follow 3 layer architecture: 
    a. Conroller Layer: Server will deligate requests from client to the appropriate controller. 
    b. Service Layer: This layer will contain core business logic. It will also directly communicate with the database layer. We do not want that server directly deligates requests to corresponding services because in that case service will have two responsibities those are validation + business logic. 
    c. Database Layer: This layer represents actual database. 

2. In my design I have implemented just the service layer. Skipped controller layer for simplicity but it is also important as it validates the request before executing core business logic for it and Database layer is not part of low level design, it is actually part of schema design. My current focus is not schema design. I have created 2 uml diagrams one showing customer facing apis and other one showing admin facing apis but the server and db instances in both are same, kept monolithic architecture for simplicity. 

3. Core API: 

getMovies(city) -> getShows(movieID) -> getSeatMap(showId) -> getBill(arrayOfSeatIds) -> makePayment(billID, method)


getTheaters(city) -> getMovies(theaterId) -> getShows(movieID) -> common

cancellation api, refund api, admin apis (will come later)

Assumptions for simplicity: 
    1. If two movies are same but shown in different theaters then their movie id will be different and they will be treated as different movies. 
    2. TicketPrice = fxn(showBasePrice, seatBasePrice, otherFactors). 
    3. Resolved: If ticket is cancelled by the user then it will still be shown booked. 
    4. Also need to think about overlapping edge cases while adding show. 
    5. Assuming that payment service will be having only one behavior. 
    6. Eviction policy is handled. 
    7. In uml diagrams it is not mentioned that how exactly dtos will look like at granular level
    8. Add screen service is not handled.

    

4. Core Services: 
    a. FetchCityMovieService: takes city and gives list of movies which are available for booking 
    b. FetchTheaterService: takes city and gives list of theaters in the city
    c. FetchTheaterMovieService: takes theater id and returns list of movies 
    d. FetchShowService: takes city, theaterID and movieId and returns shows of that movie 
    e. FetchBillService: takes array of seats and showid and other factors and returns bill
    f. MakePaymentService: takes billid and user id, creates ticket on successful payment. 
    


5. Design Patterns: 
    a. Strategy Design Pattern: There will be different pricing strategies like FiexedPriceStrategy in which ticketPrice = showBasePrice + seatBasePrice. 
    b. Once seats are selected we have to show them as booked for temporary time and if bill paid then permanently sold out. 
    c. Adapter Design Pattern: For accomandating external payment api
    d. Proxy design pattern: for every service there will be one controller that will act as proxy so that it can take care of validation and can deligate core logic to service


6. Basic Entities: 
    a. Ticket:
        TicketId: int
        User Name: String
        User Email: String
        Show Name: String
        Theater Name: String
        Auditorium Name: String
        Seat Numbers: []
    
    b. seat:
        seatId
        seatType
        seatMapId (every seatMap has it own seat ids)
        showId
        basePrice
        enum SeatStatus { AVAILABLE, HELD, BOOKED }

    c. seatMap:
        seatMapId
        showId
        list of seats 
    
    d. show:
        showId
        movieId
        auditoriumId
        theaterId
        seatMap (every show has its own seatmap)
        startTime
        endTime
        showBasePrice
    
    e. movie:
        movieId
        theaterId
        movieName
        otherDetails
    
    f. theater: 
        theaterId
        city
        otherDetails 
    
    g. user: 
        Name 
        Email
        Role
        


7. Things where I am confused?
    a. I easily feagured out the services but to implement schema design but in lld classes we never talked about schema design and it feels counter intuitive when we not talk about schema design while making core application logic. So is it standard practice to avoid schema design. (current designs does not talk about how db changes happen)
    b. Even 3 layer architecture was not mentioned.


8. Things to focus:
    a. How to handle concurrency in booking ticket
    b. How to handle overlapping and concurrency in admin operations + implement other apis as well
    




