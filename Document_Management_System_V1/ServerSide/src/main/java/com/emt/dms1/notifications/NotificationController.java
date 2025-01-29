package com.emt.dms1.notifications;

import com.emt.dms1.utils.EntityResponse;
import com.emt.dms1.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
public class NotificationController {

        @Autowired
        private NotificationService notificationService;

    @GetMapping("/current-user")
        public EntityResponse getNotificationsForCurrentUser() {
        EntityResponse entityResponse=new EntityResponse<>();

        // Get the email address of the currently logged-in user
        String currentUserEmail = SecurityUtils.getCurrentUserLogin();

        List<NotificationModel> notifications = notificationService.getNotificationsForCurrentUser(currentUserEmail);
            entityResponse.setMessage("notifications found");
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setEntity(notifications);

            return entityResponse;
        }

        @GetMapping("/currentUserSorted")
        public EntityResponse<List<NotificationModel>> getNotificationsForCurrentUser2 (){

            return notificationService.getNotificationsForCurrentUser2();
        }
        @GetMapping("/allNotifications")
        public ResponseEntity<List<NotificationModel>> getAllNotifications () {
            List<NotificationModel> notifications = notificationService.getAllNotifications();
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        }

        @PostMapping("/changeStatus")
        public EntityResponse changeStatus (@RequestParam Long id){
            return notificationService.updatenotification(id);
        }

        @GetMapping("/getStatus")
        public EntityResponse notStatus () {
            return notificationService.getNumberOfNotifications();
        }

        @GetMapping("/getusers")
        public EntityResponse fetchSharedUsers( @RequestParam String DocumentId) {
            EntityResponse entityResponse = new EntityResponse<>();

            Long parsedLong = Long.parseLong(DocumentId);
            List<String> userList =notificationService.sharedTo(parsedLong);

           entityResponse.setMessage("Displaying list of people who have this document shared with them ");
           entityResponse.setStatusCode(HttpStatus.OK.value());
           entityResponse.setEntity(userList);

           return  entityResponse;

        }


    }

//    @PostMapping
//    public ResponseEntity<NotificationModel> createNotification(@RequestBody NotificationModel notification) {
//        NotificationModel createdNotification = notificationService.createNotification(notification);
//        return new ResponseEntity<>(createdNotification, HttpStatus.CREATED);
//    }


