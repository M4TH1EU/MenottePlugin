package ch.m4th1eu.handcuffs;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HListener implements Listener {
	public Main plugin;

	public HListener(Main p) {
		plugin = p;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();

		if (!cuffed(player))
			return;

		e.setCancelled(true);
		tell(player, GREEN + "Vous ne pouvez pas faire ceci en étant menotté...");
	}

	@EventHandler
	public void onRun(PlayerMoveEvent e) {
		Player player = e.getPlayer();

		if (!cuffed(player))
			return;

		if (player.isSprinting()) {
			tell(player, GREEN + "Vous ne pouvez pas faire ceci en étant menotté...");
			player.setSprinting(false);
		}
	}

	@EventHandler
	public void onHurt(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();

			if (e.getCause() == DamageCause.FIRE || e.getCause() == DamageCause.LAVA && plugin.burnCuffs) {
				if (new Random().nextInt(20) == 0) {
					if (cuffed(player)) {
						tell(player, GREEN + "Le feu a brûlé tes menottes, tu es libre !");
						free(player);
					}
				}
			}
		}
	}

	@EventHandler
	public void onHit(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {
			Player player = (Player) e.getDamager();

			if (cuffed(player) && plugin.nerfDamage) {
				e.setDamage(e.getDamage() / 2);
			}
		}
	}

	@EventHandler
	public void onPickUp(PlayerPickupItemEvent e) {
		Player player = e.getPlayer();

		if (cuffed(player) && !plugin.canPickup)
			e.setCancelled(true);
	}

	@EventHandler
	public void onChangeInv(InventoryOpenEvent e) {
		Player player = (Player) e.getPlayer();

		if (cuffed(player) && !plugin.canChangeInv)
			e.setCancelled(true);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onClick(PlayerInteractEntityEvent e) {
		Player player = e.getPlayer();

		if (cuffed(player))
			return;

		if (e.getRightClicked() instanceof Player) {
			Player target = (Player) e.getRightClicked();

			if (!player.isOp() && plugin.reqOP)
				return;

			if (inHand(player, Material.getMaterial(plugin.cuffID))) {
				if (cuffed(target)) {
					tell(player, RED + "Ce joueur est déjà menotté");
				} else {
					if (plugin.usePerms && !player.hasPermission("hc.cuff")) {
						tell(player, RED + "Vous n'avez pas la permission.");
						return;
					}

					if (target.hasPermission("hc.immune")) {
						tell(player, ChatColor.RED + "Vous ne pouvez pas faire ça à ce joueur.");
					}

					if (inHandAmount(player) >= plugin.cuffAmount) {
						tell(target, AQUA + player.getName() + GREEN + " vous a menotté.");
						tell(player, GREEN + "Vous avez menotté " + AQUA + target.getName());
						target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999, 6));
						target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, -3));

						cuff(target);

						if (plugin.cuffTake) {
							if (player.getItemInHand().getAmount() == plugin.cuffAmount)
								player.getItemInHand().setType(Material.AIR);
							else
								player.getItemInHand()
										.setAmount(player.getItemInHand().getAmount() - plugin.cuffAmount);
						}
					} else {
						tell(player, RED + "Vous devez avoir 7 cordes en main pour menotter quelqu'un.");
					}
				}
			}

			if (inHand(player, Material.getMaterial(plugin.keyID))) {
				if (!cuffed(target)) {
					tell(player, RED + "Ce joueur est déjà libre.");

					if (plugin.keyTake) {
						if (player.getItemInHand().getAmount() == 1)
							player.getItemInHand().setType(Material.AIR);
						else
							player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
					}
				} else {
					if (plugin.usePerms && !player.hasPermission("hc.free")) {
						tell(player, RED + "Vous n'avez pas la permission.");
						return;
					}

					tell(target, AQUA + player.getName() + GREEN + " vous à liberé");
					tell(player, GREEN + "Vous avez libéré " + AQUA + target.getName() + GREEN);
					target.removePotionEffect(PotionEffectType.SLOW);
					target.removePotionEffect(PotionEffectType.BLINDNESS);
					target.removePotionEffect(PotionEffectType.JUMP);
					free(target);
				}
			}
		}
	}

	public boolean cuffed(Player player) {
		if (plugin.cuffed.contains(player))
			return true;

		return false;
	}

	public void cuff(Player player) {
		if (!cuffed(player)) {
			plugin.cuffed.add(player);
		}
	}

	public void free(Player player) {
		if (cuffed(player)) {
			plugin.cuffed.remove(player);
		}
	}

	public boolean inHand(Player player, Material m) {
		if (player.getItemInHand().getType() == m)
			return true;

		return false;
	}

	public int inHandAmount(Player p) {
		return p.getItemInHand().getAmount();
	}

	public void tell(Player p, String m) {
		p.sendMessage(GOLD + "[Menottes] " + m);
	}
}
