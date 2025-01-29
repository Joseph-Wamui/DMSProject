package com.emt.dms1.notifications;


import com.emt.dms1.user.UserRepository;
import com.emt.dms1.utils.EntityResponse;
import com.emt.dms1.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    public NotificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<NotificationModel> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public List<NotificationModel> getNotificationsForCurrentUser(String currentUserEmail) {
        return notificationRepository.findByRecipientOrderByIdDesc(currentUserEmail);
    }

    public EntityResponse getNotificationsForCurrentUser2() {
        EntityResponse entityResponse = new EntityResponse<>();

        String currentUserEmail = SecurityUtils.getCurrentUserLogin();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime startOfThreeDaysAgo = startOfToday.minusDays(3);

        List<NotificationModel> allNotifications = notificationRepository.findByRecipientOrderByIdDesc(currentUserEmail);

        // Filter unread notifications from the past three days
        List<NotificationModel> unreadNotifications = allNotifications.stream()
                .filter(notification -> notification.getStatus() == NotificationStatus.UNREAD && notification.getTimestamp().isAfter(startOfThreeDaysAgo))
                .collect(Collectors.toList());

        // Filter read notifications for today
        List<NotificationModel> readNotifications = allNotifications.stream()
                .filter(notification -> notification.getStatus() == NotificationStatus.READ && notification.getTimestamp().isAfter(startOfToday))
                .collect(Collectors.toList());

        // Combine both lists
        unreadNotifications.addAll(readNotifications);

        if (unreadNotifications.isEmpty()) {
            entityResponse.setMessage("No notifications found for the user.");
            entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        } else {
            entityResponse.setMessage("Notifications retrieved successfully.");
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setEntity(unreadNotifications);
        }

        return entityResponse;
    }

    public void createNotification(NotificationModel notification) {
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setTimestamp(LocalDateTime.now());
        notificationRepository.save(notification);
    }
    public void createNotification2(NotificationModel notification) {
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setTimestamp(LocalDateTime.now());
        notification.setDocumentId(notification.getDocumentId());
        notification.setType(notificationType.NORMAL);
        notificationRepository.save(notification);
    }
    public void createNotification3(NotificationModel notification) {
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setTimestamp(LocalDateTime.now());
        notification.setDocumentId(notification.getDocumentId());
        notification.setType(notificationType.SHARED);
        notificationRepository.save(notification);
    }

     public EntityResponse updatenotification(Long id){
         EntityResponse entityResponse=new EntityResponse<>();
         try {
             Optional<NotificationModel> notification = notificationRepository.findById(id);
             if (notification.isPresent()) {
                 NotificationModel notificationModel = notification.get();
                 notificationModel.setStatus(NotificationStatus.READ);
                 notificationRepository.save(notificationModel);
                 entityResponse.setMessage("Changed successfully ");
                 entityResponse.setStatusCode(HttpStatus.OK.value());
                 entityResponse.setEntity(notificationModel);
             } else {
                 entityResponse.setMessage("Changed failed ");
                 entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                 log.info("notification not found");
             }
         } catch (Exception e) {
             entityResponse.setMessage("Changed failed ");
             entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
              log.info("internal server error");
             e.printStackTrace();
         }
         return entityResponse;
     }

    public EntityResponse getNumberOfNotifications() {
        EntityResponse<List<NotificationStatus>> entityResponse = new EntityResponse<>();
        try {
            String email= SecurityUtils.getCurrentUserLogin();
            List<NotificationModel> notList =notificationRepository.findByRecipientOrderByIdDesc(email);
            List<NotificationStatus> statusList = new ArrayList<>();

            for (NotificationModel notificationModel:notList) {
                NotificationStatus status = notificationModel.getStatus(); // Assuming getStatus() method retrieves the status from DocumentModel
                statusList.add(status);
            }

            entityResponse.setEntity(statusList);
            entityResponse.setMessage("Status list retrieved successfully");
            entityResponse.setStatusCode(HttpStatus.OK.value());
        } catch (Exception e) {
            entityResponse.setMessage("Error occurred while retrieving status list");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            // Handle exception or log error message
        }

        return entityResponse;
    }

    public List<String> sharedTo( long documentId) {

            String email = SecurityUtils.getCurrentUserLogin();
            notificationType notificationtype = notificationType.SHARED;

            List<String> userList =  notificationRepository.findByDocumentIdAndType(documentId, notificationtype.name());

            return  userList;
    }



}
