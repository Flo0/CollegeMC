package net.collegemc.mc.libs.nametag;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import net.collegemc.mc.libs.CollegeLibrary;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NameTagListener implements Listener {

  @EventHandler
  public void onNamedEntityDespawn(EntityRemoveFromWorldEvent event) {
    NameTagManager manager = CollegeLibrary.getNameTagManager();
    if (manager.isTagged(event.getEntity().getEntityId())) {
      manager.untag(event.getEntity().getEntityId());
    }
  }

}
