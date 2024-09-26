package com.settlement.project.common.watchhistory.entity;

import com.settlement.project.common.base.Timestamped;
import com.settlement.project.common.user.entity.User;
import com.settlement.project.common.video.entity.Video;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "watch_history")
@Getter
@NoArgsConstructor
public class WatchHistory extends Timestamped {

    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "watch_history_time")
    private Integer watchHistoryTime;

    @Builder
    public WatchHistory(User user, Video video, Integer watchHistoryTime) {
        this.user = user;
        this.video = video;
        this.watchHistoryTime = watchHistoryTime;
    }

    public void updateWatchHistoryTime(int watchHistoryTime) {
        this.watchHistoryTime = watchHistoryTime;
    }


}
